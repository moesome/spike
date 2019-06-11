package com.moesome.spike.model.dao;

import com.moesome.spike.model.domain.SpikeOrder;
import com.moesome.spike.model.pojo.vo.SpikeOrderAndSpikeVo;

import java.util.List;

public interface SpikeOrderMapper {
    int deleteByPrimaryKey(Long id);

    int deleteByUserIdAndSpikeId(Long userId, Long spikeId);

    int insert(SpikeOrder record);

    int insertSelective(SpikeOrder record);

    SpikeOrder selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SpikeOrder record);

    int updateByPrimaryKey(SpikeOrder record);

    List<SpikeOrderAndSpikeVo> selectSpikeOrderAndSpikeVoByUserIdPagination(Long userId, String order, int start, int count);

    Integer countByUserId(Long userId);

    Long selectSpikeOwnerIdBySpikeOrderId(Long spikeId);
}