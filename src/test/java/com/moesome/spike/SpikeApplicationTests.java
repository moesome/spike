package com.moesome.spike;

import com.moesome.spike.model.pojo.vo.AuthVo;
import com.moesome.spike.service.AuthService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpikeApplicationTests {

	@Autowired
	private AuthService authService;

	@Autowired
	private RedisTemplate redisTemplate;
	@Test
	public void contextLoads() {

	}

	@Test
	public void getUserSessionId(){

	}

}
