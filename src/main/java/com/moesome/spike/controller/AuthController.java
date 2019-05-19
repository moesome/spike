package com.moesome.spike.controller;

import com.moesome.spike.model.vo.receive.AuthVo;
import com.moesome.spike.model.vo.result.Result;
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
	@GetMapping("check")
	public Result check(@CookieValue(required = false) String sessionId, HttpServletResponse httpServletResponse){
		return authService.check(sessionId, httpServletResponse);
	}

	/**
	 * 登录
	 * @param authVo
	 * @param httpServletResponse
	 * @return
	 */
	@PostMapping("login")
	public Result login(@Validated @RequestBody AuthVo authVo, HttpServletResponse httpServletResponse){
		return authService.login(authVo,httpServletResponse);
	}

	@PostMapping("logout")
	public Result logout(@CookieValue(required = false) String sessionId, HttpServletResponse httpServletResponse){
		return authService.logout(sessionId,httpServletResponse);
	}
}
