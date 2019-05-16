package com.moesome.spike.controller;

import com.moesome.spike.model.domain.User;
import com.moesome.spike.model.vo.OrderResult;
import com.moesome.spike.service.SpikeOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

@RestController
public class SpikeOrderController {
	@Autowired
	private SpikeOrderService spikeOrderService;

	@PostMapping("/spike_order/{spikeId}")
	public OrderResult createOrder(User user, @PathVariable Long spikeId){
		return spikeOrderService.createOrder(user,spikeId);
	}
}
