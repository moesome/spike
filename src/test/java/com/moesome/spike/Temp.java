package com.moesome.spike;

import com.moesome.spike.manager.RedisManager;
import com.moesome.spike.model.domain.Spike;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Temp {
	@Autowired
	private RedisTemplate<String,Object> redisTemplate;

	@Test
	public void test(){
		redisTemplate.opsForHash().put("test111","a",1);
		redisTemplate.opsForHash().put("test111","b",2);
		redisTemplate.opsForHash().put("test111","c",3);


	}
}
