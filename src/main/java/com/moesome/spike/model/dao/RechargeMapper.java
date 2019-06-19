package com.moesome.spike.model.dao;

import com.moesome.spike.model.domain.Recharge;

import java.util.List;

public interface RechargeMapper {
	int deleteByPrimaryKey(Long id);

	int insert(Recharge record);

	int insertSelective(Recharge record);

	List<Recharge> selectAllUnResolver();

	Recharge selectByPrimaryKey(Long id);

	int updateByPrimaryKeySelective(Recharge record);

	int updateByPrimaryKey(Recharge record);
}