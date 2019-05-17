package com.moesome.spike.service;

import com.moesome.spike.exception.message.SuccessCode;
import com.moesome.spike.model.dao.SpikeMapper;
import com.moesome.spike.model.domain.Spike;
import com.moesome.spike.model.vo.SpikeResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class SpikeService {
	@Autowired
	private SpikeMapper spikeMapper;

	@Autowired
	private RedisTemplate<String,Object> redisTemplate;

	public SpikeResult index(String order, int page){
		if (StringUtils.isEmpty(order) || order.equals("ascend")){
			order = "ASC";
		}else{
			order = "DESC";
		}
		if (page < 0){
			page = 1;
		}
		List<Spike> spikeList = spikeMapper.selectByPagination(order, (page - 1) * 10, 10);
		int count = spikeMapper.count();
		return new SpikeResult(SuccessCode.OK,spikeList, count);
	}

	/**
	 * 根据 id 查秒杀项目
	 * @param spikeId
	 * @return
	 */
	public Spike getSpikeById(Long spikeId){
		return spikeMapper.selectByPrimaryKey(spikeId);
	}

	public boolean decrementStock(Long spikeId){
		return spikeMapper.decrementStockById(spikeId) > 0;
	}

	/**
	 * 优化秒杀，将秒杀要用到的一些参数写入缓存，如果这些值能通过验证再写入数据库
	 */
	public void init() {
		List<Spike> spikes = spikeMapper.selectAll();
		for (Spike spike : spikes){
			redisTemplate.opsForHash().put("spike"+spike.getId(),"stock",spike.getStock());
			redisTemplate.opsForHash().put("spike"+spike.getId(),"startAt",spike.getStartAt());
			redisTemplate.opsForHash().put("spike"+spike.getId(),"endAt",spike.getEndAt());
//			System.out.println("1:"+redisTemplate.opsForHash().get("spike"+spike.getId(),"stock"));
//			System.out.println("2:"+redisTemplate.opsForHash().get("spike"+spike.getId(),"startAt"));
//			System.out.println("3:"+redisTemplate.opsForHash().get("spike"+spike.getId(),"endAt"));
		}
	}
}
