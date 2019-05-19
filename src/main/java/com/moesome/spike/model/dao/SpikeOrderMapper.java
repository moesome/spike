package com.moesome.spike.model.dao;

import com.moesome.spike.model.domain.SpikeOrder;
import com.moesome.spike.model.vo.receive.SpikeOrderAndSpikeVo;

import java.util.List;

public interface SpikeOrderMapper {
    int deleteByPrimaryKey(Long id);

    int insert(SpikeOrder record);

    int insertSelective(SpikeOrder record);

    SpikeOrder selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SpikeOrder record);

    int updateByPrimaryKey(SpikeOrder record);

    List<SpikeOrderAndSpikeVo> selectByUserIdPagination(Long userId, String order, int start, int count);

    int countByUserId(Long userId);
}