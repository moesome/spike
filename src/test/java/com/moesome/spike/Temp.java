package com.moesome.spike;

import com.moesome.spike.manager.inter.DistributedLock;
import com.moesome.spike.service.SpikeService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Temp {
	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private SpikeService spikeService;

	@Test
	public void test(){

//		String script = "return redis.call('bf.add',KEYS[1],ARGV[1])";
//		DefaultRedisScript<Boolean> redisScript = new DefaultRedisScript<>(script, Boolean.class);
//		Boolean user4 = redisTemplate.execute(redisScript, Collections.singletonList("boolean:aaron:test"), String.valueOf("user"+id));
	}
}
