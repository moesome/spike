package com.moesome.spike.controller;

import com.moesome.spike.model.domain.User;
import com.moesome.spike.model.pojo.vo.SpikeVo;
import com.moesome.spike.model.pojo.result.Result;
import com.moesome.spike.service.SpikeService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
public class SpikeController implements InitializingBean {
	@Autowired
	private SpikeService spikeService;

	@GetMapping("spikes")
	public Result index(int page, String order){
		return spikeService.index(order,page);
	}

	@GetMapping("spikes/manage")
	public Result manage(User user, int page, String order){
		return spikeService.manage(user,order,page);
	}

	@GetMapping("spikes/{id}")
	public Result show(User user,@PathVariable Long id){
		return spikeService.show(user,id);
	}

	@PostMapping("spikes")
	public Result store(User user,@RequestBody @Validated SpikeVo spikeVo){
		return spikeService.store(user,spikeVo);
	}

	@PatchMapping("spikes/{id}")
	public Result update(User user, @PathVariable Long id,@RequestBody @Validated SpikeVo spikeVo){
		return spikeService.update(user,spikeVo,id);
	}
	// 暂未开放
	//@DeleteMapping("/spikes/{id}")
	public Result delete(User user, @PathVariable Long id){
		return spikeService.delete(user,id);
	}

	@Override
	public void afterPropertiesSet(){
		spikeService.init();
	}
}
