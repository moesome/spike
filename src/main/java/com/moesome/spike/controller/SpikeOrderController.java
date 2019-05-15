package com.moesome.spike.controller;

import com.moesome.spike.model.domain.User;
import com.moesome.spike.service.SpikeOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SpikeOrderController {
	@Autowired
	private SpikeOrderService spikeOrderService;

	@PostMapping("/spike_order/{spikeId}")
	public String createOrder(User user, @PathVariable Long spikeId){
		System.out.println(user);
		System.out.println(spikeId);
		spikeOrderService.createOrder(user,spikeId);
		return "ok";
	}
}
