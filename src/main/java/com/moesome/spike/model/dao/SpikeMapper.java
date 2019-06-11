package com.moesome.spike.model.dao;

import com.moesome.spike.model.domain.Spike;import com.moesome.spike.model.pojo.vo.SpikeAndUserContactWayVo;

import java.math.BigDecimal;
import java.util.List;

public interface SpikeMapper {
	int deleteByPrimaryKey(Long id);

	int insert(Spike record);

	int insertSelective(Spike record);

	Spike selectByPrimaryKey(Long id);

	BigDecimal selectPriceByPrimaryKey(Long id);

	int updateByPrimaryKeySelective(Spike record);

	int updateByPrimaryKey(Spike record);

	List<Spike> selectAll();

	List<Spike> selectByPagination(String order, int start, int count);

	List<Spike> selectByUserIdPagination(Long id, String order, int start, int count);

	SpikeAndUserContactWayVo selectSpikeAndUserContactWayBySpikeId(Long spikeId);

	int decrementStockById(Long id);

	Integer count();

	Integer countByUserId();
}