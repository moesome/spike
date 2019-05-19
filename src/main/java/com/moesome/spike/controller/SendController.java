package com.moesome.spike.controller;

import com.moesome.spike.model.domain.User;
import com.moesome.spike.model.pojo.result.Result;
import com.moesome.spike.service.SendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SendController {
	@Autowired
	private SendService sendService;

	@GetMapping("/send/remind/{id}")
	public Result remindToSendProduction(User user, @PathVariable Long id){
		return sendService.remindToSendProduction(user, id);
	}

	// 获取该用户相关的被秒杀订单，连同商品信息返回（分页）
	@GetMapping("/sends")
	public Result index(int page, String order,User user){
		return sendService.index(page,order,user);
	}
}
