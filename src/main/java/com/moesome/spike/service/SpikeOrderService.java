package com.moesome.spike.service;

import com.moesome.spike.exception.message.SuccessCode;
import com.moesome.spike.model.dao.SpikeMapper;
import com.moesome.spike.model.dao.SpikeOrderMapper;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;
import java.util.List;


@Service
public class SpikeOrderService {
	//private static final Object lock = new Object();
	@Autowired
	private CommonService commonService;

	@Autowired
	private RedisService redisService;

	@Autowired
	private SpikeMapper spikeMapper;

	@Autowired
	private SpikeOrderMapper spikeOrderMapper;

	// 取预减库存使用
	@Autowired
	private RedisTemplate<String, Integer> redisTemplate;

	// 存储订单结果，供下单后轮询使用
	@Autowired
	private RedisTemplate<String, SpikeOrder> redisTemplateForSpikeOrder;

	// 校验用户是否已经发出了请求，防止多次请求带来的阻塞
	@Autowired
	private RedisTemplate<String, SpikeOrderVo> redisTemplateForSpikeOrderVo;

	// 取日期使用
	@Autowired
	private RedisTemplate<String, Object> redisTemplateForSpike;

	@Autowired
	private MQSender mqSender;

	@Autowired
	private TransactionTemplate transactionTemplate;



	public static final SpikeOrder ORDER_FAILED = new SpikeOrder(-1L,null,null,null,null);
	/*
	以下为并发可能出现错误的代码：
	发生原因为启动了事务，可能多个事务在进入锁之前开启，查询时使用的视图可能没有及时被更新
	这时可以在 select 后加上 lock in share mode （加读锁）或 for update（加写锁），加锁后变成当前读。但效率会降低
	@Transactional
	public OrderResult createOrder(User user,Long spikeId){
		// 检查 id 是否传过来了
		if (spikeId == null)
			return OrderResult.REQUEST_ERR;
		// 检查是否登录
		if (user == null)
			return OrderResult.UNAUTHORIZED;
		synchronized(lock){
			// 查库存
			Spike spike = spikeService.getSpikeById(spikeId);
			// 判断是否大于 0
			if (spike.getStock() <= 0){
				return OrderResult.LIMIT_EXCEED;
			}
			// 使用了用户 id 和秒杀项目 id 的唯一索引，无需校验是否已经抢过，直接下订单，下单失败则说明已经抢过
			// 下订单
			SpikeOrder spikeOrder = new SpikeOrder(null, user.getId(), spike.getId(), new Date(), (byte) 1);
			spikeOrderMapper.insert(spikeOrder);
			// 减库存
			spikeService.decrementStock(spikeId);
		}
		return new OrderResult(SuccessCode.OK);
	}*/

	/**
	 * 1. 初始化时，预加载所有秒杀项目到 redis，查询时从 redis 读出数据（主界面查询时并发不高，仍用数据库来查）
	 * 2. 收到请求时，redis 库存减一，如果还大于 0 则将秒杀请求加入队列（读写分离）
	 * 3. 请求出队列，进行处理
	 * 4. 客户端轮询，检查秒杀是否成功
	 * 注意，以上流程存在缓存不一致问题
	 * @param user
	 * @param spikeOrderVo
	 * @return
	 */
	public Result store(User user,SpikeOrderVo spikeOrderVo) {
		// 检查是否登录
		if (user == null)
			return AuthResult.UNAUTHORIZED;
		spikeOrderVo.setUserId(user.getId());
		Long spikeId = spikeOrderVo.getSpikeId();
		// 阻止多次重复下单
		SpikeOrderVo spikeOrderVoInRedis = redisTemplateForSpikeOrderVo.opsForValue().get(generateSpikeOrderVoKey(spikeOrderVo));
		if (spikeOrderVoInRedis != null){
			// 已经下过单了
			return OrderResult.REPEATED_REQUEST;
		}else{
			redisTemplateForSpikeOrderVo.opsForValue().set(generateSpikeOrderVoKey(spikeOrderVo),spikeOrderVo);
		}
		// 查库存到减订单加锁，已经用预减缓存策略优化掉了该锁
		// synchronized (lock) {
		// 查库存优化为直接对库存进行原子自增（减一），如果减少后大于 0 则说明还能下单
		// 减缓存库存
		Long stockRemain = redisTemplate.opsForHash().increment("spike" + spikeId, "stock", -1);
		if (stockRemain < 0){
			return OrderResult.LIMIT_EXCEED;
		}
		Date startAt = (Date)redisTemplateForSpike.opsForHash().get("spike" + spikeId, "startAt");
		Date endAt = (Date)redisTemplateForSpike.opsForHash().get("spike" + spikeId, "endAt");

		if (startAt == null||endAt == null){
			// redis 查出的结果无效则还是在数据库中取
			Spike spike = spikeMapper.selectByPrimaryKey(spikeId);
			startAt = spike.getStartAt();
			endAt = spike.getEndAt();
		}
		Date now = new Date();
		// 在开始之前或结束之后则直接返回错误码
		if (now.compareTo(startAt) < 0 ){
			return OrderResult.TIME_LIMIT_NOT_ARRIVED;
		}
		if (now.compareTo(endAt) > 0){
			return OrderResult.TIME_LIMIT_EXCEED;
		}

		// }
		// 使用了用户 id 和秒杀项目 id 的唯一索引，无需校验是否已经抢过，直接下订单，下单失败则说明已经抢过

		orderAndDecrementStock(user, spikeId);
		return new OrderResult(SuccessCode.OK);
	}

	/*
	优化为加入队列后执行操作
	@Transactional
	boolean OrderAndDecrementStock(User user, Long spikeId){
		// 下订单
		SpikeOrder spikeOrder = new SpikeOrder(null, user.getId(), spikeId, new Date(), (byte) 1);
		spikeOrderMapper.insert(spikeOrder);
		// 减库存
		spikeService.decrementStock(spikeId);
		return true;
	}*/

	/**
	 * 发送将要生成的订单关键信息到队列
	 * @param user
	 * @param spikeId
	 */
	private void orderAndDecrementStock(User user, Long spikeId){
		mqSender.sendToSpikeTopic(new SpikeOrderVo(user.getId(),spikeId));
	}

	// 开启 retry 后，异常会被捕获并用于重试，无法被声明式事务操作捕获，此处手动管理事务
	//	@Transactional

	/**
	 * 处理从队列收到的订单信息
	 * 供 rabbitmq 的 received 调用
	 * @param spikeOrderVo
	 */
	@Retryable(value= {Exception.class},maxAttempts = 2)
	void resolveOrder(SpikeOrderVo spikeOrderVo){
		// 使用声明式事务管理来解决 retry 和 transaction 冲突问题
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				try{
					// System.out.println("减库存");
					// 减库存
					boolean decrementStock = spikeMapper.decrementStockById(spikeOrderVo.getSpikeId()) > 0;
					if (!decrementStock){
						System.out.println("减库存失败");
						return;
					}
					// 下订单
					SpikeOrder spikeOrder = new SpikeOrder(null, spikeOrderVo.getUserId(), spikeOrderVo.getSpikeId(), new Date(), (byte) 1);
					insert(spikeOrder);
					// System.out.println("下订单");
					// 订单加入缓存
					// spikeOrder 主键已由 mybatis 在插入成功后自动注入
					redisTemplateForSpikeOrder.opsForValue().set(generateSpikeOrderKey(spikeOrder),spikeOrder);
					// 刷新第一页缓存
					redisService.reCacheFirstPage();
				}catch (Exception e){
					// System.out.println("发生异常，进行回滚");
					status.setRollbackOnly();
					// 抛出错误让重试框架捕获，错误两次则会由 recover 方法进行处理
					throw e;
				}
			}
		});
	}




	@Recover
	void resolverOrderFailed(Exception e,SpikeOrderVo spikeOrderVo){
		// System.out.println("重试失败，交易未成功！");
		// 未成功交易应该给缓存中加一
		SpikeOrder spikeOrder = new SpikeOrder(null, spikeOrderVo.getUserId(), spikeOrderVo.getSpikeId(), null, (byte) 1);
		redisTemplate.opsForHash().increment("spike" + spikeOrderVo.getSpikeId(), "stock", 1);
		redisTemplateForSpikeOrder.opsForValue().set(generateSpikeOrderKey(spikeOrder),ORDER_FAILED);
	}

	private String generateSpikeOrderKey(SpikeOrder spikeOrder){
		return "spikeOrder-userId:"+spikeOrder.getUserId()+"-spikeId:"+spikeOrder.getSpikeId();
	}

	private String generateSpikeOrderVoKey(SpikeOrderVo spikeOrderVo){
		return "spikeOrderVo-userId:"+spikeOrderVo.getUserId()+"-spikeId:"+spikeOrderVo.getSpikeId();
	}

	private void insert(SpikeOrder spikeOrder) {
		spikeOrderMapper.insert(spikeOrder);
	}

	public Result check(User user, Long spikeId) {
		if (user == null)
			return AuthResult.UNAUTHORIZED;
		SpikeOrder spikeOrder = new SpikeOrder(null, user.getId(), spikeId, null, (byte) 1);
		spikeOrder = redisTemplateForSpikeOrder.opsForValue().get(generateSpikeOrderKey(spikeOrder));
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
