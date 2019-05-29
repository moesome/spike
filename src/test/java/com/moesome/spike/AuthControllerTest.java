package com.moesome.spike;

import com.moesome.spike.controller.AuthController;
import com.moesome.spike.model.dao.UserMapper;
import com.moesome.spike.model.domain.User;
import com.moesome.spike.model.pojo.result.Result;
import com.moesome.spike.model.pojo.vo.AuthVo;
import com.moesome.spike.util.EncryptUtil;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AuthControllerTest {
	@Autowired
	private AuthController authController;

	@Autowired
	private UserMapper userMapper;

	private User user;

	private AuthVo authVo;

	private static MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

	@Before
	public void createUser(){
		user = new User();
		user.setUsername("test"+Math.random());
		user.setPassword(EncryptUtil.md5(EncryptUtil.md5("1")));
		user.setCreatedAt(new Date());
		user.setUpdatedAt(new Date());
		user.setEmail("1@qq.com");
		user.setPhone("13125469875");
		user.setNickname("testName");
		int insert = userMapper.insert(user);
		Assert.assertEquals("创建用户错误", 1, insert);
	}

	@After
	public void deleteUser(){
		int delete = userMapper.deleteByPrimaryKey(user.getId());
		Assert.assertEquals("删除用户错误", 1, delete);
	}
	@Test
	public void test_01_login(){
		authVo = new AuthVo();
		authVo.setUsername(user.getUsername());
		authVo.setPassword(EncryptUtil.md5("1"));
		Result result = authController.login(authVo, mockHttpServletResponse);
		Assert.assertEquals("登录失败", 0, (long)result.getCode());
	}
	@Test
	public void test_02_check(){
		String sessionId = mockHttpServletResponse.getCookie("sessionId").getValue();
		Result check = authController.check(sessionId, mockHttpServletResponse);
		Assert.assertEquals("检查登录失败", 0, (long)check.getCode());
	}
	@Test
	public void test_03_logout(){
		String sessionId = mockHttpServletResponse.getCookie("sessionId").getValue();
		Result logout = authController.logout(sessionId, mockHttpServletResponse);
		Assert.assertEquals("推出登录失败", 0, (long)logout.getCode());

	}
}
