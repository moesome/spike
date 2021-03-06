package com.moesome.spike.service;

import com.moesome.spike.exception.message.SuccessCode;
import com.moesome.spike.manager.DistributedLockForSpike;
import com.moesome.spike.manager.MQSenderManager;
import com.moesome.spike.manager.RedisManager;
import com.moesome.spike.manager.inter.DistributedLock;
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
import com.moesome.spike.model.pojo.vo.SpikePriceAndStockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
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
	private RedisManager redisManager;

	@Autowired
	private DistributedLockForSpike distributedLockForSpike;

	@Autowired
	private SpikeMapper spikeMapper;

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private SpikeOrderMapper spikeOrderMapper;

	@Autowired
	private MQSenderManager mqSenderManager;

	@Autowired
	private TransactionTemplate transactionTemplate;

	private static final RuntimeException ORDER_FAILED = new RuntimeException();

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
		spikeOrderVo.setSessionId(sessionId);
		spikeOrderVo.setUserId(user.getId());
		Long spikeId = spikeOrderVo.getSpikeId();
		// 阻止多次重复下单 1
		if (!redisManager.saveSpikeOrderVo(spikeOrderVo)){
			return OrderResult.REPEATED_REQUEST;
		}
		// 预减库存 2
		if(redisManager.testStock(spikeId)){
			if (!redisManager.decrementStock(spikeId)){
				return OrderResult.LIMIT_EXCEED;
			}
		}// 直接跳过，等接下来的步骤重建缓存

		Spike spikeInRedis = redisManager.getSpike(spikeId);
		// 防止缓存击穿、雪崩时对数据库造成过大压力
		if (spikeInRedis.getStartAt() == null||spikeInRedis.getEndAt() == null||spikeInRedis.getPrice() == null){
			// 加刷新锁确保只有一个线程可能会查数据库，同时确保缓存一致性问题，即保证查数据、刷新缓存单线程执行
			distributedLockForSpike.lockSpike(spikeId);
			// 重新校验是否被其他线程刷新了
			spikeInRedis = redisManager.getSpike(spikeId);
			if (spikeInRedis.getStartAt() == null||spikeInRedis.getEndAt() == null||spikeInRedis.getPrice() == null) {
				// redis 查出的结果无效则还是在数据库中取
				spikeInRedis = spikeMapper.selectByPrimaryKey(spikeId);
				// 刷新缓存
				redisManager.saveSpike(spikeInRedis);
			}
			distributedLockForSpike.unlockSpike(spikeId);
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
				redisManager.saveUser(user,sessionId);
			}else{
				// 金币不足，不合法，请求能发送过来金币一定是够的
				return OrderResult.REQUEST_ERR;
			}
		}
		// 使用队列削峰，快速返回结果——“排队中”
		// 免费商品直接加入队列
		orderAndDecrementStock(spikeOrderVo);
		return new OrderResult(SuccessCode.OK);
	}


	/**
	 * 发送将要生成的订单关键信息到队列
	 * @param spikeOrderVo
	 */
	private void orderAndDecrementStock(SpikeOrderVo spikeOrderVo){
		mqSenderManager.sendToSpikeTopic(spikeOrderVo);
	}

	/**
	 * 处理从队列收到的订单信息
	 * 供 rabbitmq 的 received 调用
	 * @param spikeOrderVo
	 */
	public void resolveOrder(SpikeOrderVo spikeOrderVo){
		try{
			// 加锁，解决超卖等问题，同时缓存一致性问题可以解决
			distributedLockForSpike.lockSpike(spikeOrderVo.getSpikeId());
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					// 取数据库数据
					SpikePriceAndStockVo spikePriceAndStockVo = spikeMapper.selectPriceAndStockByPrimaryKey(spikeOrderVo.getSpikeId());
					if (spikePriceAndStockVo.getPrice().compareTo(BigDecimal.ZERO) > 0){
						// 减金币
						boolean decrementCoin = userMapper.decrementCoinById(spikePriceAndStockVo.getPrice(), spikeOrderVo.getUserId()) > 0;
						if (!decrementCoin){
							System.out.println("减金币失败");
							throw ORDER_FAILED;
						}
					}// 售价不大于零不用减金币

					if (spikePriceAndStockVo.getStock() > 0){
						// 减库存
						boolean decrementStock = spikeMapper.decrementStockById(spikeOrderVo.getSpikeId()) > 0;
						if (!decrementStock){
							System.out.println("减库存失败");
							throw ORDER_FAILED;
						}
					}else{
						// 库存小于等于零直接终止处理
						throw ORDER_FAILED;
					}
					// 下订单
					SpikeOrder spikeOrder = new SpikeOrder(null, spikeOrderVo.getUserId(), spikeOrderVo.getSpikeId(), new Date(), (byte) 1,spikePriceAndStockVo.getPrice());
					spikeOrderMapper.insert(spikeOrder);
					// 订单加入缓存，用于轮询时查询 4
					// spikeOrder 主键已由 mybatis 在插入成功后自动注入
					redisManager.saveSpikeOrder(spikeOrder,true);
					// TODO :写日志，暂时输出到控制台
					System.out.println("spike"+spikeOrderVo.getSpikeId()+"处理完成，处理时间："+new Date()+"处理时剩余库存："+spikePriceAndStockVo.getStock()+"售价："+spikePriceAndStockVo.getPrice());
				}
			});
		// 捕获重复下单异常
		}catch (DuplicateKeyException e){
			System.out.println("与唯一索引冲突");
			// 与 resolverOrderFailed 区别是这里仍然需要禁止重复下单
			SpikeOrder spikeOrder = new SpikeOrder(null, spikeOrderVo.getUserId(), spikeOrderVo.getSpikeId(), null, (byte) 1,null);
			// 取消预减库存 2
			redisManager.incrementStock(spikeOrderVo.getSpikeId());
			// 取消缓存中减少的金币 3
			redisManager.saveUser(userMapper.selectByPrimaryKey(spikeOrderVo.getUserId()),spikeOrderVo.getSessionId());
			// 设置订单失败轮询消息 4
			redisManager.saveSpikeOrder(spikeOrder,false);
		}catch (Exception e){
			resolverOrderFailed(spikeOrderVo);
		}finally {
			distributedLockForSpike.unlockSpike(spikeOrderVo.getSpikeId());
		}
	}

	// 执行该方法时，锁还未释放
	private void resolverOrderFailed(SpikeOrderVo spikeOrderVo){
		// 未成功交易应该给缓存中加一，并取消缓存中的订单，给轮询的订单设置状态，设置轮询消息只用到了用户 id 和商品 id
		SpikeOrder spikeOrder = new SpikeOrder(null, spikeOrderVo.getUserId(), spikeOrderVo.getSpikeId(), null, (byte) 1,null);
		// 取消禁止重复下单 1
		redisManager.removeSpikeOrderVo(spikeOrderVo);
		// 取消预减库存 2
		redisManager.incrementStock(spikeOrderVo.getSpikeId());
		// 取消缓存中减少的金币 3
		redisManager.saveUser(userMapper.selectByPrimaryKey(spikeOrderVo.getUserId()),spikeOrderVo.getSessionId());
		// 设置订单失败轮询消息 4
		redisManager.saveSpikeOrder(spikeOrder,false);
	}


	public Result check(User user, Long spikeId) {
		// 取轮询只用了用户 id 和商品 id
		SpikeOrder spikeOrder = new SpikeOrder(null, user.getId(), spikeId, null, (byte) 1,null);
		spikeOrder = redisManager.getSpikeOrder(spikeOrder);
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
		int p = commonService.pageFormat(page);
		String o = commonService.orderFormat(order);
		List<SpikeOrderAndSpikeVo> spikeOrderAndSpikeVos = spikeOrderMapper.selectSpikeOrderAndSpikeVoByUserIdPagination(user.getId(),o, (p - 1) * 10, 10);
		Integer count = spikeOrderMapper.countByUserId(user.getId());
		return new SpikeOrderResult(SuccessCode.OK,spikeOrderAndSpikeVos,count);
	}

	public Result delete(User user, Long id,Long spikeId) {
		int delete = spikeOrderMapper.deleteByPrimaryKey(id, user.getId());
		if (delete != 1){
			// 删除失败
			return OrderResult.REQUEST_ERR;
		}
		// 清缓存订单创建过程中产生的缓存
		// 清除：取消禁止重复下单 1
		SpikeOrderVo spikeOrderVo = new SpikeOrderVo();
		spikeOrderVo.setUserId(user.getId());
		spikeOrderVo.setSpikeId(spikeId);
		redisManager.removeSpikeOrderVo(spikeOrderVo);
		// 清除：订单加入缓存，用于轮询时查询 4
		SpikeOrder spikeOrder = new SpikeOrder();
		spikeOrder.setUserId(user.getId());
		spikeOrder.setSpikeId(spikeId);
		redisManager.removeSpikeOrder(spikeOrder);
		return OrderResult.OK_WITHOUT_BODY;
	}
}
