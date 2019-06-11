package com.moesome.spike;

import com.moesome.spike.controller.UserController;
import com.moesome.spike.model.domain.User;
import com.moesome.spike.model.pojo.result.Result;
import com.moesome.spike.model.pojo.vo.UserVo;
import com.moesome.spike.service.UserService;
import com.moesome.spike.util.EncryptUtil;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserControllerTest {
	@Autowired
	private TestCommon testCommon;

	@Autowired
	private UserController userController;

	@Autowired
	private UserService userService;

	private User user;

	private static MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

	@Before
	public void createUserAndLogin(){
		user = testCommon.createUser();
		testCommon.login(user,mockHttpServletResponse);
	}

	@After
	public void deleteUser(){
		testCommon.deleteUser();
		System.out.println("del");
		testCommon.logout(mockHttpServletResponse);
	}

	@Test
	public void show(){
		Result result = userController.show(user, user.getId());
		Assert.assertEquals("index 错误", 0, (long)result.getCode());
	}

	@Test
	public void store(){
		UserVo userVo = new UserVo();
		userVo.setUsername("test"+Math.random());
		userVo.setEmail("sda@qq.com");
		userVo.setNickname("testNick");
		userVo.setPassword(EncryptUtil.md5(EncryptUtil.md5("1")));
		userVo.setPhone("12345678963");
		Result store = userController.store(userVo);
		User user = (User)(store.getObject());
		delete(user.getId());
	}

	private void delete(Long id){
		userService.delete(mockHttpServletResponse.getCookie("sessionId").getValue(),id);
	}

	@Test
	public void update(){
		UserVo userVo = new UserVo();
		userVo.setUsername("test"+Math.random());
		userVo.setEmail("s22@qq.com");
		userVo.setNickname("testNick");
		userVo.setPassword(EncryptUtil.md5(EncryptUtil.md5("1")));
		userVo.setPhone("12345678963");
		userController.update(mockHttpServletResponse.getCookie("sessionId").getValue(),user,user.getId(),userVo,mockHttpServletResponse);
	}
}
