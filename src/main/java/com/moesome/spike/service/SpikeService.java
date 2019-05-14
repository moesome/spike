package com.moesome.spike.service;

import com.moesome.spike.model.dao.SpikeMapper;
import com.moesome.spike.model.domain.Spike;
import com.moesome.spike.model.vo.SpikeResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class SpikeService {
	@Autowired
	SpikeMapper spikeMapper;

	public SpikeResult index(String order, int page){
		if (StringUtils.isEmpty(order) || order.equals("ascend")){
			order = "ASC";
		}else{
			order = "DESC";
		}
		if (page < 0){
			page = 1;
		}
		List<Spike> spikeList = spikeMapper.selectByPagination(order, (page - 1) * 10, 10);
		int count = spikeMapper.count();
		System.out.println(count);
		return new SpikeResult(spikeList, count);
	}
}
