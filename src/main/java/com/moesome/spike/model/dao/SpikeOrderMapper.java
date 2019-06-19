package com.moesome.spike.model.dao;

import com.moesome.spike.model.domain.SpikeOrder;import com.moesome.spike.model.pojo.vo.SpikeOrderAndSpikeVo;

import java.math.BigDecimal;
import java.util.List;

public interface SpikeOrderMapper {
	int deleteByPrimaryKey(Long id);

	int insert(SpikeOrder record);

	int insertSelective(SpikeOrder record);

	SpikeOrder selectByPrimaryKey(Long id);

	int updateByPrimaryKeySelective(SpikeOrder record);

	int updateByPrimaryKey(SpikeOrder record);

	// 只有下单人才能删除
	int deleteByPrimaryKey(Long id, Long userId);

	int deleteByUserIdAndSpikeId(Long userId, Long spikeId);

	List<SpikeOrderAndSpikeVo> selectSpikeOrderAndSpikeVoByUserIdPagination(Long userId, String order, int start, int count);

	Integer countByUserId(Long userId);

	Long selectSpikeOwnerIdBySpikeOrderId(Long spikeId);

	BigDecimal selectPriceByPrimaryKey(Long id);
}