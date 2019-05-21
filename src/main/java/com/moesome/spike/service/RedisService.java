package com.moesome.spike.service;

import com.moesome.spike.config.RedisConfig;
import com.moesome.spike.model.dao.SpikeMapper;
import com.moesome.spike.model.domain.Spike;
import com.moesome.spike.model.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 存放公用 redis 操作
 */
@Service
public class RedisService {
	@Autowired
	private RedisTemplate<String, User> redisTemplate;

	@Autowired
	private RedisTemplate<String, List<Spike>> redisTemplateForFirstPage;

	@Autowired
	private RedisTemplate<String, Integer> redisTemplateForInteger;

	@Autowired
	private SpikeMapper spikeMapper;

	/**
	 * redis 根据 sessionId 读出 User
	 * @param sessionId
	 * @return
	 */
	public User getUserBySessionId(String sessionId){
		return redisTemplate.opsForValue().get(sessionId);
	}

	/**
	 * 刷新 redis session 的缓存存在时间
	 * @param sessionId
	 */
	public void refreshMsgInRedis(String sessionId, int time){
		redisTemplate.expire(sessionId,time, TimeUnit.SECONDS);
	}

	/**
	 * 将用户存入 redis
	 * @param user
	 * @return
	 */
	public String saveUserAndGenerateSessionId(User user){
		String sessionId = UUID.randomUUID().toString().replace("-","");
		redisTemplate.opsForValue().set(sessionId, user, RedisConfig.EXPIRE_SECOND,TimeUnit.SECONDS);
		return sessionId;
	}

	/**
	 * 刷新秒杀商品第一页缓存
	 */
	public void reCacheFirstPage(){
		// System.out.println("刷新商品第一页缓存");
		// 缓存商品第一页
		redisTemplateForFirstPage.opsForValue().set("firstSpikePage",spikeMapper.selectByPagination("DESC", 0, 10));
		// 缓存总数
		redisTemplateForInteger.opsForValue().set("count",spikeMapper.count());
	}
}
