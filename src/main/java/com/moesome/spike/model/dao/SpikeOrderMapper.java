package com.moesome.spike.model.dao;

import com.moesome.spike.model.domain.SpikeOrder;

public interface SpikeOrderMapper {
    int deleteByPrimaryKey(Long id);

    int insert(SpikeOrder record);

    int insertSelective(SpikeOrder record);

    SpikeOrder selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SpikeOrder record);

    int updateByPrimaryKey(SpikeOrder record);
}