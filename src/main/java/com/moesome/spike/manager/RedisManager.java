package com.moesome.spike.manager;

import com.moesome.spike.config.RedisConfig;
import com.moesome.spike.model.dao.SpikeMapper;
import com.moesome.spike.model.domain.Spike;
import com.moesome.spike.model.domain.SpikeOrder;
import com.moesome.spike.model.domain.User;
import com.moesome.spike.model.pojo.vo.SpikeOrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 存放公用 redis 操作
 */
@Service
public class RedisManager {
	public static final Object SPIKE_REFRESH_LOCK = new Object();

	@Autowired
	private RedisTemplate<String, User> redisTemplateForUser;

	@Autowired
	private RedisTemplate<String,Object> redisTemplateForSpike;

	// 存储订单结果，供下单后轮询使用
	@Autowired
	private RedisTemplate<String, SpikeOrder> redisTemplateForSpikeOrder;

	// 校验用户是否已经发出了请求，防止多次请求带来的阻塞
	@Qualifier("stringRedisTemplate")
	@Autowired
	private RedisTemplate<String, String> redisTemplateForSpikeOrderVo;

	@Autowired
	private RedisTemplate<String,Long> redisTemplateForRechargeId;


	public static final SpikeOrder ORDER_FAILED = new SpikeOrder(-1L,null,null,null,null,null);

	public boolean saveRechargeId(Long id){
		return redisTemplateForRechargeId.opsForSet().add("rechargeId",id) == 1;
	}

	public void removeRechargeId(Long id){
		redisTemplateForRechargeId.opsForSet().remove("rechargeId",id);
	}

	/**
	 * 将下单信息存入 redis，flag == true 表示成功，flag == false 表示失败，用于客户端轮询检测下单结果
	 * @param spikeOrder
	 * @param isSuccess
	 */
	public void saveSpikeOrder(SpikeOrder spikeOrder,boolean isSuccess){
		if (isSuccess)
			redisTemplateForSpikeOrder.opsForValue().set(generateSpikeOrderKey(spikeOrder),spikeOrder,60*60, TimeUnit.SECONDS);
		else
			redisTemplateForSpikeOrder.opsForValue().set(generateSpikeOrderKey(spikeOrder),ORDER_FAILED,60*60, TimeUnit.SECONDS);
	}


	public void removeSpikeOrder(SpikeOrder spikeOrder){
		redisTemplateForSpikeOrder.expire(generateSpikeOrderKey(spikeOrder),0, TimeUnit.SECONDS);
	}

	/**
	 * 取出下单信息
	 * 1.若取出值为空表示还在队列，未对该请求进行处理。
	 * 2.若取出值的 id 为 -1 表示处理失败
	 * 3.其他情况为下单请求处理成功
	 * @param spikeOrder
	 * @return
	 */
	public SpikeOrder getSpikeOrder(SpikeOrder spikeOrder){
		return redisTemplateForSpikeOrder.opsForValue().get(generateSpikeOrderKey(spikeOrder));
	}

	/**
	 * 将下单请求存入 redis set，防止重复下单
	 * @param spikeOrderVo
	 * @return
	 */
	public boolean saveSpikeOrderVo(SpikeOrderVo spikeOrderVo){
		return redisTemplateForSpikeOrderVo.opsForSet().add("spike_order_vo", generateSpikeOrderVoValue(spikeOrderVo)) == 1;
	}

	/**
	 * 从 redis set 中移除该订单
	 * @param spikeOrderVo
	 */
	public void removeSpikeOrderVo(SpikeOrderVo spikeOrderVo){
		redisTemplateForSpikeOrderVo.opsForSet().remove("spike_order_vo", generateSpikeOrderVoValue(spikeOrderVo));
	}

	private String generateSpikeOrderVoValue(SpikeOrderVo spikeOrderVo){
		return "spikeOrder-userId:"+spikeOrderVo.getUserId()+"spikeOrder-spikeId:"+spikeOrderVo.getSpikeId();
	}

	private String generateSpikeOrderKey(SpikeOrder spikeOrder){
		return "spikeOrder-userId:"+spikeOrder.getUserId()+"-spikeId:"+spikeOrder.getSpikeId();
	}

	public boolean testStock(Long spikeId){
		return redisTemplateForSpike.opsForHash().get("spike"+spikeId,"stock") != null;
	}

	/**
	 * 预减库存，成功减少库存返回 true
	 * @param spikeId
	 * @return
	 */
	public boolean decrementStock(Long spikeId){
		return redisTemplateForSpike.opsForHash().increment("spike" + spikeId, "stock", -1) >= 0;
	}

	/**
	 * 库存加一
	 * @param spikeId
	 */
	public void incrementStock(Long spikeId){
		redisTemplateForSpike.opsForHash().increment("spike" + spikeId, "stock", 1);
	}

	/**
	 * 保存商品部分信息（stock,price,startAt,endAt）到 redis，用于下单前检测
	 * @param spike
	 */
	public void saveSpike(Spike spike){
		HashMap<String, Object> stringObjectHashMap = new HashMap<>(9);
		stringObjectHashMap.put("name",spike.getName());
		stringObjectHashMap.put("userId",spike.getUserId());
		stringObjectHashMap.put("detail",spike.getDetail());
		stringObjectHashMap.put("stock",spike.getStock());
		stringObjectHashMap.put("price",spike.getPrice());
		stringObjectHashMap.put("startAt",spike.getStartAt());
		stringObjectHashMap.put("endAt",spike.getEndAt());
		stringObjectHashMap.put("createdAt",spike.getCreatedAt());
		stringObjectHashMap.put("updatedAt",spike.getUpdatedAt());
		redisTemplateForSpike.opsForHash().putAll("spike"+spike.getId(),stringObjectHashMap);
	}

	/**
	 * 取出商品部分信息（price,startAt,endAt）
	 * @param spikeId
	 * @return
	 */
	public Spike getSpike(Long spikeId){
		// 从缓存取信息用于校验
		ArrayList<Object> list = new ArrayList<>(9);
		list.add("name");
		list.add("userId");
		list.add("detail");
		list.add("stock");
		list.add("price");
		list.add("startAt");
		list.add("endAt");
		list.add("createdAt");
		list.add("updatedAt");
		List<Object> multiGet = redisTemplateForSpike.opsForHash().multiGet("spike" + spikeId,list);
		Spike spike = new Spike();
		spike.setName((String)multiGet.get(0));
		spike.setUserId(Long.valueOf(multiGet.get(1).toString()));
		spike.setDetail((String) multiGet.get(2));
		spike.setStock((Integer) multiGet.get(3));
		spike.setPrice((BigDecimal) multiGet.get(4));
		spike.setStartAt((Date)multiGet.get(5));
		spike.setEndAt((Date)multiGet.get(6));
		spike.setCreatedAt((Date)multiGet.get(7));
		spike.setUpdatedAt((Date)multiGet.get(8));
		return spike;
	}

	/**
	 * redis 根据 sessionId 读出 User
	 * @param sessionId
	 * @return
	 */
	public User getUserBySessionId(String sessionId){
		return redisTemplateForUser.opsForValue().get(sessionId);
	}

	/**
	 * 刷新 redis session 的缓存存在时间
	 * @param sessionId
	 */
	public void refreshUser(String sessionId, int time){
		redisTemplateForUser.expire(sessionId,time, TimeUnit.SECONDS);
	}

	/**
	 * 将用户存入 redis 并生成 sessionId
	 * @param user
	 * @return
	 */
	public String saveUserAndGenerateSessionId(User user){
		String sessionId = UUID.randomUUID().toString().replace("-","");
		redisTemplateForUser.opsForValue().set(sessionId, user, RedisConfig.EXPIRE_SECOND,TimeUnit.SECONDS);
		return sessionId;
	}

	/**
	 * 将用户存入 redis
	 * @param user
	 * @return
	 */
	public String saveUser(User user,String sessionId){
		redisTemplateForUser.opsForValue().set(sessionId, user, RedisConfig.EXPIRE_SECOND,TimeUnit.SECONDS);
		return sessionId;
	}
	/*public void reCacheFirstPage(List<Spike> firstPage,int count){
		synchronized (RedisManager.FIRSTPAGE_REFRESH_LOCK){
			cacheFirstPage(firstPage);
			cachePageCount(count);
		}
	}

	*//**
	 * 刷新秒杀商品第一页缓存
	 *//*
	public void reCacheFirstPage(){
		// System.out.println("刷新商品第一页缓存");
		// 缓存商品第一页
		reCacheFirstPage(spikeMapper.selectByPagination("DESC", 0, 10),spikeMapper.count());
	}*/

	/**
	 * 从 redis 移除 spike
	 * @param spike
	 */
	public void removeSpike(Spike spike){
		redisTemplateForSpike.delete("spike"+spike.getId());
	}

	/*public void cacheFirstPage(List<Spike> firstPage){
		// 缓存商品第一页
		redisTemplateForFirstPage.opsForValue().set("firstSpikePage",firstPage);
	}

	public List<Spike> getFirstPage(){
		return redisTemplateForFirstPage.opsForValue().get("firstSpikePage");
	}

	public void cachePageCount(int i){
		// 缓存总数
		redisTemplateForSpike.opsForValue().set("pageCount",i);
	}

	public Integer getPageCount(){
		return (Integer)redisTemplateForSpike.opsForValue().get("pageCount");
	}
*/
	public Boolean lockSpike(Long spikeId){
		return redisTemplateForSpike.opsForValue().setIfAbsent("lock:spike-"+spikeId,true,10,TimeUnit.SECONDS);
	}

	public Boolean unlockSpike(Long spikeId){
		return redisTemplateForSpike.delete("lock:spike-"+spikeId);
	}
}
