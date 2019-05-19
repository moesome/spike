package com.moesome.spike.model.dao;

import com.moesome.spike.model.domain.Spike;
import com.moesome.spike.model.pojo.vo.SpikeAndUserContactWayVo;

import java.util.List;

public interface SpikeMapper {
    List<Spike> selectAll();

    List<Spike> selectByPagination(String order,int start,int count);

    List<Spike> selectByUserIdPagination(Long id,String order,int start,int count);

    SpikeAndUserContactWayVo selectSpikeAndUserContactWayBySpikeId(Long spikeId);

    int decrementStockById(Long id);

    int count();

    int countByUserId();

    int deleteByPrimaryKey(Long id);

    int insertSelective(Spike record);

    Spike selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Spike record);
}