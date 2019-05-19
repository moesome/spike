package com.moesome.spike.controller;

import com.moesome.spike.model.domain.User;
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
	public Result show(User user,@PathVariable Long id){
		return userService.show(user,id);
	}

	@PostMapping("/users")
	public Result store(@RequestBody @Validated UserVo userVo){
		return userService.store(userVo);
	}

	@PatchMapping("users/{id}")
	public Result update(@CookieValue(required = false) String sessionId, User user, @PathVariable Long id, @RequestBody @Validated UserVo userVo, HttpServletResponse httpServletResponse){
		return userService.update(sessionId,user,userVo,id,httpServletResponse);
	}
}
