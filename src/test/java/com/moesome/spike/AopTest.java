package com.moesome.spike;

import com.moesome.spike.model.dao.UserMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@Service
public class AopTest {
	@Autowired
	private UserMapper userMapper;

	@Autowired
	private RedisTemplate<String,Object> redisTemplateForSpike;

	@Test
	public void e(){
		redisTemplateForSpike.opsForValue().setIfAbsent("lock:spike-"+2,true,10, TimeUnit.SECONDS);
	}


}
