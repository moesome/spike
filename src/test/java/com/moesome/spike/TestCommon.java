package com.moesome.spike;

import com.moesome.spike.controller.AuthController;
import com.moesome.spike.model.dao.SpikeMapper;
import com.moesome.spike.model.dao.UserMapper;
import com.moesome.spike.model.domain.Spike;
import com.moesome.spike.model.domain.User;
import com.moesome.spike.model.pojo.result.Result;
import com.moesome.spike.model.pojo.vo.AuthVo;
import com.moesome.spike.util.EncryptUtil;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import java.math.BigDecimal;
import java.util.Date;

@Component
public class TestCommon {
	@Autowired
	private UserMapper userMapper;

	private User user;

	@Autowired
	private AuthController authController;

	@Autowired
	private SpikeMapper spikeMapper;

	private Spike spike;

	public Spike createSpike(int stock,Date startAt,Date endAt){
		spike = new Spike();
		spike.setUserId(user.getId());
		spike.setStock(stock);
		Date date = new Date();
		spike.setStartAt(startAt);
		spike.setEndAt(endAt);
		spike.setDetail("Test");
		spike.setCreatedAt(date);
		spike.setName("test"+Math.random());
		spike.setUpdatedAt(date);
		int insert = spikeMapper.insertSelective(spike);
		Assert.assertEquals("商品用户错误", 1, insert);

		return spike;
	}

	public User createUser(){
		user = new User();
		user.setUsername("test"+Math.random());
		user.setPassword(EncryptUtil.md5(EncryptUtil.md5("1")));
		user.setCreatedAt(new Date());
		user.setUpdatedAt(new Date());
		user.setEmail("1053770594@qq.com");
		user.setPhone("13125469875");
		user.setNickname("testName");
		user.setCoin(new BigDecimal(666));
		int insert = userMapper.insert(user);
		Assert.assertEquals("创建用户错误", 1, insert);
		return user;
	}

	public void deleteUser(){
		int delete = userMapper.deleteByPrimaryKey(user.getId());
		Assert.assertEquals("删除用户错误", 1, delete);
	}

	public void deleteSpike() {
		int delete = spikeMapper.deleteByPrimaryKey(spike.getId());
		Assert.assertEquals("删除商品错误", 1, delete);
	}

	public void login(User user, MockHttpServletResponse mockHttpServletResponse){
		AuthVo authVo = new AuthVo();
		authVo.setUsername(user.getUsername());
		authVo.setPassword(EncryptUtil.md5("1"));
		Result result = authController.login(authVo, mockHttpServletResponse);
		Assert.assertEquals("登录失败", 0, (long)result.getCode());
	}

	public void check(MockHttpServletResponse mockHttpServletResponse){
		String sessionId = mockHttpServletResponse.getCookie("sessionId").getValue();
		Result check = authController.check(sessionId, mockHttpServletResponse);
		Assert.assertEquals("检查登录失败", 0, (long)check.getCode());
	}

	public void logout(MockHttpServletResponse mockHttpServletResponse){
		String sessionId;
		for (Cookie cookie : mockHttpServletResponse.getCookies()){
			sessionId = cookie.getValue();
			authController.logout(sessionId, mockHttpServletResponse);
		}
	}
}
