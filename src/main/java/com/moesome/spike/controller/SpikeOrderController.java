package com.moesome.spike.controller;

import com.moesome.spike.model.domain.User;
import com.moesome.spike.model.vo.result.Result;
import com.moesome.spike.model.vo.receive.SpikeOrderVo;
import com.moesome.spike.service.SpikeOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class SpikeOrderController {
	@Autowired
	private SpikeOrderService spikeOrderService;

	@GetMapping("/spike_orders")
	public Result index(User user, int page, String order){
		return spikeOrderService.index(user,order,page);
	}

	// 此处传入的 vo 仅仅接收秒杀 id 即可，无需校验用户名是否为空，用户名在 session 内提取
	@PostMapping("/spike_orders")
	public Result store(User user, @RequestBody SpikeOrderVo spikeOrderVo){
		System.out.println(spikeOrderVo);
		return spikeOrderService.store(user,spikeOrderVo.getSpikeId());
	}

	@GetMapping("/spike_orders/check/{spikeId}")
	public Result check(User user, @PathVariable Long spikeId){
		return spikeOrderService.check(user,spikeId);
	}
	
}
