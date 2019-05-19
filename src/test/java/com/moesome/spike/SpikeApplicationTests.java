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
		AuthVo authVo1 = new AuthVo("1","c4ca4238a0b923820dcc509a6f75849b");
		AuthVo authVo2 = new AuthVo("2","c4ca4238a0b923820dcc509a6f75849b");
		AuthVo authVo3 = new AuthVo("3","c4ca4238a0b923820dcc509a6f75849b");
		AuthVo authVo4 = new AuthVo("4","c4ca4238a0b923820dcc509a6f75849b");
		AuthVo authVo5 = new AuthVo("5","c4ca4238a0b923820dcc509a6f75849b");
		AuthVo authVo6 = new AuthVo("6","c4ca4238a0b923820dcc509a6f75849b");
		AuthVo authVo7 = new AuthVo("7","c4ca4238a0b923820dcc509a6f75849b");
		AuthVo authVo8 = new AuthVo("8","c4ca4238a0b923820dcc509a6f75849b");
		AuthVo authVo9 = new AuthVo("9","c4ca4238a0b923820dcc509a6f75849b");
		System.out.println(authService.login(authVo1,null));
		System.out.println(authService.login(authVo2,null));
		System.out.println(authService.login(authVo3,null));
		System.out.println(authService.login(authVo4,null));
		System.out.println(authService.login(authVo5,null));
		System.out.println(authService.login(authVo6,null));
		System.out.println(authService.login(authVo7,null));
		System.out.println(authService.login(authVo8,null));
		System.out.println(authService.login(authVo9,null));
		for (Object key : redisTemplate.keys("*")) {
			System.out.println(key);
		}

	}

}
