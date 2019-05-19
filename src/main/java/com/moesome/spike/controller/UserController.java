package com.moesome.spike.controller;

import com.moesome.spike.model.domain.User;
import com.moesome.spike.model.vo.result.UserResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
	@PostMapping("/users")
	public UserResult store(@RequestBody User user){
		System.out.println(user);
		return null;
	}
}
