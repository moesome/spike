package com.moesome.spike.controller;

import com.moesome.spike.model.vo.SpikeResult;
import com.moesome.spike.service.SpikeService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SpikeController implements InitializingBean {
	@Autowired
	private SpikeService spikeService;

	@GetMapping("spike/index/{page}")
	public SpikeResult index(@PathVariable int page, String order){
		return spikeService.index(order,page);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		spikeService.init();
	}
}
