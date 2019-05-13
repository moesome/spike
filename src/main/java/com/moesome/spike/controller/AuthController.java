package com.moesome.spike.controller;

import com.moesome.spike.model.vo.AuthResult;
import com.moesome.spike.model.vo.AuthVo;
import com.moesome.spike.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;


@RestController
public class AuthController {
	@Autowired
	private AuthService authService;

	/**
	 * 检验用户是否登录
	 * @param sessionId
	 * @return
	 */
	@GetMapping("/check")
	public AuthResult check(@CookieValue(required = false) String sessionId,HttpServletResponse httpServletResponse){
		return authService.check(sessionId, httpServletResponse);
	}

	/**
	 * 登录
	 * @param authVo
	 * @param httpServletResponse
	 * @return
	 */
	@PostMapping("/login")
	public AuthResult login(@Validated @RequestBody AuthVo authVo, HttpServletResponse httpServletResponse){
		return authService.login(authVo,httpServletResponse);
	}
}
