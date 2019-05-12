package com.moesome.spike.controller;

import com.moesome.spike.model.po.User;
import com.moesome.spike.model.vo.LoginResult;
import com.moesome.spike.model.vo.LoginVo;
import com.moesome.spike.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;


@RestController
@CrossOrigin("*")
public class LoginController {
	@Autowired
	private LoginService loginService;

	@PostMapping("/login")
	public LoginResult login(@Validated @RequestBody LoginVo loginVo, HttpServletResponse httpServletResponse){
		return loginService.login(loginVo,httpServletResponse);
	}
}
