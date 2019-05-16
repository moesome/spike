package com.moesome.spike.model.dao;

import com.moesome.spike.model.domain.Spike;import java.util.List;

public interface SpikeMapper {
    List<Spike> selectAll();

    List<Spike> selectByPagination(String order,int start,int count);

    int decrementStockById(Long id);

    int count();

    int deleteByPrimaryKey(Long id);

    int insertSelective(Spike record);

    Spike selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Spike record);
}