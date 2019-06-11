package com.moesome.spike.service;

import com.moesome.spike.exception.message.SuccessCode;
import com.moesome.spike.model.dao.SpikeMapper;
import com.moesome.spike.model.dao.SpikeOrderMapper;
import com.moesome.spike.model.dao.UserMapper;
import com.moesome.spike.model.domain.Spike;
import com.moesome.spike.model.domain.SpikeOrder;
import com.moesome.spike.model.domain.User;
import com.moesome.spike.model.pojo.vo.SpikeOrderAndSpikeVo;
import com.moesome.spike.model.pojo.vo.SpikeOrderVo;
import com.moesome.spike.model.pojo.result.AuthResult;
import com.moesome.spike.model.pojo.result.OrderResult;
import com.moesome.spike.model.pojo.result.Result;
import com.moesome.spike.model.pojo.result.SpikeOrderResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


@Service
public class SpikeOrderService {

	@Autowired
	private CommonService commonService;

	@Autowired
	private RedisService redisService;

	@Autowired
	private SpikeMapper spikeMapper;

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private SpikeOrderMapper spikeOrderMapper;

	@Autowired
	private MQSender mqSender;

	@Autowired
	private TransactionTemplate transactionTemplate;

	/**
	 * 1. 初始化时，预加载所有秒杀项目到 redis，查询时从 redis 读出数据（主界面查询时并发不高，仍用数据库来查）
	 * 2. 收到请求时，redis 库存减一，如果还大于 0 则将秒杀请求加入队列（读写分离）
	 * 3. 请求出队列，进行处理
	 * 4. 客户端轮询，检查秒杀是否成功
	 * @param user
	 * @param spikeOrderVo
	 * @return
	 */
	public Result store(User user,SpikeOrderVo spikeOrderVo,String sessionId) {
		// 检查是否登录
		if (user == null)
			return AuthResult.UNAUTHORIZED;
		spikeOrderVo.setUserId(user.getId());
		Long spikeId = spikeOrderVo.getSpikeId();
		// 阻止多次重复下单 1
		if (!redisService.saveSpikeOrderVo(spikeOrderVo)){
			return OrderResult.REPEATED_REQUEST;
		}
		// 预减库存 2
		if (!redisService.decrementStock(spikeId)){
			return OrderResult.LIMIT_EXCEED;
		}
		Spike spikeInRedis = redisService.getSpike(spikeId);
		if (spikeInRedis.getStartAt() == null||spikeInRedis.getEndAt() == null||spikeInRedis.getPrice() == null){
			// redis 查出的结果无效则还是在数据库中取
			spikeInRedis = spikeMapper.selectByPrimaryKey(spikeId);
			// 刷新缓存
			redisService.removeSpike(spikeInRedis);
		}
		Date now = new Date();
		// 在开始之前或结束之后则直接返回错误码
		if (now.compareTo(spikeInRedis.getStartAt()) < 0 ){
			return OrderResult.TIME_LIMIT_NOT_ARRIVED;
		}
		if (now.compareTo(spikeInRedis.getEndAt()) > 0){
			return OrderResult.TIME_LIMIT_EXCEED;
		}
		if (spikeInRedis.getPrice().compareTo(BigDecimal.ZERO) > 0){
			// 收费商品预减价格 3
			user.setCoin(user.getCoin().subtract(spikeInRedis.getPrice()));
			if (user.getCoin().compareTo(BigDecimal.ZERO) >= 0){
				redisService.saveUser(user,sessionId);
			}else{
				// 金币不足，不合法，请求能发送过来金币一定是够的
				return OrderResult.REQUEST_ERR;
			}
		}
		// 免费商品直接加入队列
		orderAndDecrementStock(user,spikeId,sessionId);
		return new OrderResult(SuccessCode.OK);
	}


	/**
	 * 发送将要生成的订单关键信息到队列
	 * @param user
	 * @param spikeId
	 */
	private void orderAndDecrementStock(User user, Long spikeId,String sessionId){
		mqSender.sendToSpikeTopic(new SpikeOrderVo(user.getId(),spikeId,sessionId));
	}

	/**
	 * 处理从队列收到的订单信息
	 * 供 rabbitmq 的 received 调用
	 * @param spikeOrderVo
	 */
	void resolveOrder(SpikeOrderVo spikeOrderVo){
		try{
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus status) {
						// 取数据库价格
						BigDecimal price = spikeMapper.selectPriceByPrimaryKey(spikeOrderVo.getSpikeId());
						// 减金币
						boolean decrementCoin = userMapper.decrementCoinById(price, spikeOrderVo.getUserId()) > 0;
						if (!decrementCoin){
							System.out.println("减金币失败");
							resolverOrderFailed(spikeOrderVo);
							return;
						}
						// 减库存
						boolean decrementStock = spikeMapper.decrementStockById(spikeOrderVo.getSpikeId()) > 0;
						if (!decrementStock){
							System.out.println("减库存失败");
							resolverOrderFailed(spikeOrderVo);
							return;
						}
						// 下订单
						SpikeOrder spikeOrder = new SpikeOrder(null, spikeOrderVo.getUserId(), spikeOrderVo.getSpikeId(), new Date(), (byte) 1);
						insert(spikeOrder);
						// 订单加入缓存，用于轮询时查询
						// spikeOrder 主键已由 mybatis 在插入成功后自动注入
						redisService.saveSpikeOrder(spikeOrder,true);
						// 刷新第一页缓存
						redisService.reCacheFirstPage();
				}
			});
		}catch (Exception e){
			e.printStackTrace();
			resolverOrderFailed(spikeOrderVo);
		}
	}

	private void resolverOrderFailed(SpikeOrderVo spikeOrderVo){
		// System.out.println("重试失败，交易未成功！");
		// 未成功交易应该给缓存中加一，并取消缓存中的订单，给轮询的订单设置状态
		SpikeOrder spikeOrder = new SpikeOrder(null, spikeOrderVo.getUserId(), spikeOrderVo.getSpikeId(), null, (byte) 1);
		// 取消禁止重复下单 1
		redisService.removeSpikeOrderVo(spikeOrderVo);
		// 取消预减库存 2
		redisService.incrementStock(spikeOrderVo.getSpikeId());
		// 取消缓存中减少的金币 3
		redisService.saveUser(userMapper.selectByPrimaryKey(spikeOrderVo.getUserId()),spikeOrderVo.getSessionId());
		// 设置订单失败轮询消息
		redisService.saveSpikeOrder(spikeOrder,false);

	}

	private void insert(SpikeOrder spikeOrder) {
		spikeOrderMapper.insert(spikeOrder);
	}

	public Result check(User user, Long spikeId) {
		if (user == null)
			return AuthResult.UNAUTHORIZED;
		SpikeOrder spikeOrder = new SpikeOrder(null, user.getId(), spikeId, null, (byte) 1);
		spikeOrder = redisService.getSpikeOrder(spikeOrder);
		if (spikeOrder == null){
			// 请求还在队列中
			return OrderResult.IN_QUEUE;
		}else{
			if (spikeOrder.getId() == -1){
				// 请求失败
				return OrderResult.FAILED;
			}else{
				return new OrderResult(SuccessCode.OK,spikeOrder);
			}
		}
	}

	public Result index(User user, String order, int page) {
		if (user == null)
			return AuthResult.UNAUTHORIZED;
		int p = commonService.pageFormat(page);
		String o = commonService.orderFormat(order);
		List<SpikeOrderAndSpikeVo> spikeOrderAndSpikeVos = spikeOrderMapper.selectSpikeOrderAndSpikeVoByUserIdPagination(user.getId(),o, (p - 1) * 10, 10);
		Integer count = spikeOrderMapper.countByUserId(user.getId());
		return new SpikeOrderResult(SuccessCode.OK,spikeOrderAndSpikeVos,count);
	}

}
