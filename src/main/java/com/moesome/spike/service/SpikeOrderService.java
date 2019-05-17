package com.moesome.spike.service;

import com.fasterxml.jackson.databind.annotation.JsonAppend;
import com.moesome.spike.exception.message.SuccessCode;
import com.moesome.spike.model.dao.SpikeOrderMapper;
import com.moesome.spike.model.domain.Spike;
import com.moesome.spike.model.domain.SpikeOrder;
import com.moesome.spike.model.domain.User;
import com.moesome.spike.model.vo.OrderResult;
import com.moesome.spike.model.vo.SpikeOrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;


@Service
public class SpikeOrderService {
	private static final Object lock = new Object();

	@Autowired
	private SpikeService spikeService;

	@Autowired
	private SpikeOrderMapper spikeOrderMapper;

	@Autowired
	private RedisTemplate<String,Integer> redisTemplate;

	@Autowired
	private MQSender mqSender;

	@Autowired
	private TransactionTemplate transactionTemplate;
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
	 * @param spikeId
	 * @return
	 */
	public OrderResult createOrder(User user,Long spikeId) {
		// 检查 id 是否传过来了
		if (spikeId == null)
			return OrderResult.REQUEST_ERR;
		// 检查是否登录
		if (user == null)
			return OrderResult.UNAUTHORIZED;
		// 查库存到减订单加锁
		synchronized (lock) {
			// 查库存
			Integer stock = (Integer)redisTemplate.opsForHash().get("spike" + spikeId, "stock");
			Date startAt = (Date)redisTemplate.opsForHash().get("spike" + spikeId, "startAt");
			Date endAt = (Date)redisTemplate.opsForHash().get("spike" + spikeId, "endAt");
			if (stock == null||startAt == null||endAt == null){
				// redis 查出的结果无效则还是在数据库中取
				Spike spike = spikeService.getSpikeById(spikeId);
				stock = spike.getStock();
				startAt = spike.getStartAt();
				endAt = spike.getEndAt();
				System.out.println("查数据库");
			}else{
				System.out.println("查缓存");
			}
			System.out.println(stock);
			System.out.println(startAt);
			System.out.println(endAt);

			Date now = new Date();
			// 在开始之前或结束之后则直接返回错误码
			if (now.compareTo(startAt) < 0 ){
				return OrderResult.TIME_LIMIT_NOT_ARRIVED;
			}
			if (now.compareTo(endAt) > 0){
				return OrderResult.TIME_LIMIT_EXCEED;
			}
			// 判断是否大于 0
			if (stock <= 0) {
				return OrderResult.LIMIT_EXCEED;
			}else{
				//减缓存库存
				redisTemplate.opsForHash().increment("spike" + spikeId,"stock",-1);
			}
		}
		// 使用了用户 id 和秒杀项目 id 的唯一索引，无需校验是否已经抢过，直接下订单，下单失败则说明已经抢过
		/**
		 * 由于预减库存工作已经在 redis 里完成，这一步可以移出同步代码块
		 * 此时为直接提交事务，之后修改为提交至队列
		 */
		OrderAndDecrementStock(user, spikeId);
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

	void OrderAndDecrementStock(User user, Long spikeId){
		mqSender.sendToSpikeTopic(new SpikeOrderVo(user.getId(),spikeId));
	}

	// 开启 retry 后，异常会被捕获并用于重试，无法被声明式事务操作捕获，此处手动管理事务
	//	@Transactional
	@Retryable(value= {Exception.class},maxAttempts = 3)
	void ResolveOrder(SpikeOrderVo spikeOrderVo){
		/**
		 *
		 *
		 *
		 *
		 * ===========使用声明式事务管理来解决 retry 和 transaction 冲突问题
		 *
		 *
		 */

		System.out.println("减库存");
		// 减库存
		boolean decrementStock = spikeService.decrementStock(spikeOrderVo.getSpike_id());
		if (!decrementStock){
			System.out.println("减库存失败");
			return;
		}
		// 下订单
		SpikeOrder spikeOrder = new SpikeOrder(null, spikeOrderVo.getUser_id(), spikeOrderVo.getSpike_id(), new Date(), (byte) 1);
		insert(spikeOrder);
		System.out.println("下订单");
	}
	@Recover
	void ResolverOrderFailed(Exception e,SpikeOrderVo spikeOrderVo){
		System.out.println("=======================fail");
	}



	public void insert(SpikeOrder spikeOrder) {
		spikeOrderMapper.insert(spikeOrder);
	}
}
