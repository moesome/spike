package com.moesome.spike.controller;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;

@Controller
public class TestController {

	@Autowired
	private RedisTemplate<String, String> redisTemplate;


	@GetMapping("/test")
	public void test(){
		redisTemplate.opsForValue().set("hello","world");
	}


}
