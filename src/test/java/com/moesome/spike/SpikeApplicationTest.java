package com.moesome.spike;

import com.moesome.spike.controller.AuthController;
import com.moesome.spike.controller.SpikeController;
import com.moesome.spike.controller.UserController;
import com.moesome.spike.model.domain.Spike;
import com.moesome.spike.model.domain.User;
import com.moesome.spike.model.pojo.result.Result;
import com.moesome.spike.model.pojo.result.SpikeResult;
import com.moesome.spike.model.pojo.vo.AuthVo;
import com.moesome.spike.model.pojo.vo.SpikeVo;
import com.moesome.spike.model.pojo.vo.UserVo;
import com.moesome.spike.util.EncryptUtil;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;

import javax.servlet.http.Cookie;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SpikeApplicationTest {
	@Autowired
	private UserController userController;

	@Autowired
	private AuthController authController;

	private static MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

	private User user;

	@Autowired
	private SpikeController spikeController;

	@Before
	public void before(){
		// 创建用户
		UserVo userVo = new UserVo();
		userVo.setUsername("test"+Math.random());
		userVo.setEmail("1053770594@qq.com");
		userVo.setNickname("testNick");
		userVo.setPassword(EncryptUtil.md5(EncryptUtil.md5("1")));
		userVo.setPhone("12345678963");
		Result store = userController.store(userVo);
		Assert.assertEquals("创建用户错误", 0, (long)store.getCode());
		// 登录
		AuthVo authVo = new AuthVo();
		authVo.setUsername(userVo.getUsername());
		authVo.setPassword(EncryptUtil.md5("1"));
		Result result = authController.login(authVo, mockHttpServletResponse);
		Assert.assertEquals("登录失败", 0, (long)result.getCode());
		// 校验登录
		String sessionId = mockHttpServletResponse.getCookie("sessionId").getValue();
		Result check = authController.check(sessionId, mockHttpServletResponse);
		Assert.assertEquals("检查登录失败", 0, (long)check.getCode());
		// 存储用户
		user = (User)check.getObject();
	}

	@Test
	public void spikeTest(){
		// 创建 spike
		SpikeVo spikeVo = new SpikeVo();
		spikeVo.setStock(2);
		spikeVo.setEndAt(new Date(System.currentTimeMillis()+100000));
		spikeVo.setStartAt(new Date());
		spikeVo.setDetail("test");
		spikeVo.setName("test" + Math.random());
		Result result = spikeController.store(user, spikeVo);
		Assert.assertEquals("store 测试失败", 0, (long)result.getCode());
		// 修改 spike
		// 查询 spike
		// 删除 spike
	}









	@After
	public void after(){
		// 退出登录
		String sessionId;
		for (Cookie cookie : mockHttpServletResponse.getCookies()){
			sessionId = cookie.getValue();
			authController.logout(sessionId, mockHttpServletResponse);
		}
		// 删除用户
		userController.delete("",user.getId());
	}
}
