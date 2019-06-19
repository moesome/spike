package com.moesome.spike.controller;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.moesome.spike.model.domain.User;
import com.moesome.spike.model.pojo.result.AuthResult;
import com.moesome.spike.model.pojo.vo.UserVo;
import com.moesome.spike.model.pojo.result.Result;
import com.moesome.spike.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
public class UserController {
	@Autowired
	private UserService userService;

	@GetMapping("/users/{id}")
	public Result show(@CookieValue(required = false) String sessionId, User user,@PathVariable Long id){
		if (user == null)
			return AuthResult.UNAUTHORIZED;
		if (!user.getId().equals(id)) {
			return AuthResult.AUTH_FAILED;
		}
		return userService.show(user,sessionId);
	}

	@PostMapping("/users")
	public Result store(@RequestBody @Validated UserVo userVo){
		return userService.store(userVo);
	}

	@PatchMapping("users/{id}")
	public Result update(@CookieValue(required = false) String sessionId, User user, @PathVariable Long id, @RequestBody @Validated UserVo userVo){
		return userService.update(sessionId,user,userVo,id);
	}

	public Result delete(String sessionId,Long id){
		return userService.delete(sessionId,id);
	}
}
