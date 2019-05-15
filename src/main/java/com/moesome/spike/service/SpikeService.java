package com.moesome.spike.service;

import com.moesome.spike.exception.message.SuccessCode;
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
		return new SpikeResult(SuccessCode.OK,spikeList, count);
	}

	/**
	 * 根据 id 查秒杀项目
	 * @param spickId
	 * @return
	 */
	public Spike getSpikeById(Long spickId){
		return spikeMapper.selectByPrimaryKey(spickId);
	}

	public boolean decrementStock(Long spickId){
		return spikeMapper.decrementStockById(spickId) > 0;
	}

}
