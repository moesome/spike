package com.moesome.spike.controller;

import com.moesome.spike.model.domain.User;
import com.moesome.spike.model.pojo.result.Result;
import com.moesome.spike.service.SendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 收发礼物
 */
@RestController
public class SendController {
	@Autowired
	private SendService sendService;

	@PatchMapping("/sends/remind/{spikeOrderId}")
	public Result remindToSendProduction(User user, @PathVariable Long spikeOrderId){
		return sendService.remindToSendProduction(user, spikeOrderId);
	}

	@PatchMapping("/sends/received/{spikeOrderId}")
	public Result receivedProduction(User user, @PathVariable Long spikeOrderId){
		return sendService.receivedProduction(user, spikeOrderId);
	}

	@PatchMapping("/sends/{spikeOrderId}")
	public Result sendProduction(User user, @PathVariable Long spikeOrderId){
		return sendService.sendProduction(user, spikeOrderId);
	}

	// 获取该用户相关的被秒杀订单，连同商品信息返回（分页）
	@GetMapping("/sends")
	public Result index(int page, String order,User user){
		return sendService.index(page,order,user);
	}
}
