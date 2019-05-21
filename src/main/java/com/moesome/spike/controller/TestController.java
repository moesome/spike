package com.moesome.spike.controller;

import com.moesome.spike.model.domain.User;
import com.moesome.spike.model.pojo.vo.UserVo;
import com.moesome.spike.service.RedisService;
import com.moesome.spike.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
public class TestController {
	@Autowired
	private UserService userService;

	@Autowired
	private RedisService redisService;

	@Autowired
	private RedisTemplate<String,User> redisTemplate;

	// 创建测试用户，在创建前请删除所有测试用户
	@PostMapping("c4ca4238a0b923820dcc509a6f75849b")
	public String createTestUser(){
		User test0 = userService.getUserByUserName("测试0");
		if (test0 != null)
			return "生成失败，请先删除所有测试用户";
		for (int i = 0;i < 500;i++){
			UserVo userVo = new UserVo("测试"+i,"测试名"+i,"c4ca4238a0b923820dcc509a6f75849b","test"+i+"@qq.com","12345678911");
			userService.store(userVo);
		}
		return "生成成功";
	}

	@PostMapping("getsessionid-c4ca4238a0b923820dcc509a6f75849b")
	public LinkedList<String> getTestUserSessionId(){
		LinkedList<String> list = new LinkedList<>();
		for (int i = 0;i < 500;i++){
			String sessionId = redisService.saveUserAndGenerateSessionId(userService.getUserByUserName("测试" + i));
			list.add(sessionId);
		}
		return list;
	}

	public String saveUserAndGenerateSessionId(User user){
		String sessionId = UUID.randomUUID().toString().replace("-","");
		// 测试用户 sessionId 5个小时过期
		redisTemplate.opsForValue().set(sessionId, user, 3600*5, TimeUnit.SECONDS);
		return sessionId;
	}
}
