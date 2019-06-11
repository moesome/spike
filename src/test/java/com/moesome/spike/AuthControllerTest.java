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
import org.springframework.context.annotation.Lazy;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AuthControllerTest {
	@Autowired
	private TestCommon testCommon;

	private User user;

	private static MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

	@Before
	public void createUser(){
		user = testCommon.createUser();
	}

	@After
	public void deleteUser(){
		testCommon.deleteUser();
	}
	@Test
	public void auth(){
		testCommon.login(user,mockHttpServletResponse);
		testCommon.check(mockHttpServletResponse);
		testCommon.logout(mockHttpServletResponse);
	}
}
