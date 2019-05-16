package com.moesome.spike.controller;

import com.moesome.spike.model.domain.User;
import com.moesome.spike.service.MQReceiver;
import com.moesome.spike.service.MQSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
	@Autowired
	private MQSender mqSender;

	@Autowired
	private MQReceiver mqReceiver;

	@RequestMapping("mq")
	public String mq(){
		User user = new User();
		user.setId(66);
		mqSender.send(user);
		return "ok";
	}
}
