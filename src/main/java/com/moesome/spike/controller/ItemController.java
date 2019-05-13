package com.moesome.spike.controller;

import com.moesome.spike.model.po.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ItemController {

	@PatchMapping("item/{itemId}")
	public User modify(@PathVariable String itemId, User user){
		return user;
	}
}
