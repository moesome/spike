package com.moesome.spike.model.domain;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SpikeOrder implements Order{
    private Long id;

    private Integer userId;

    private Long spikeId;

    private Date createdAt;

    /**
    * 1.正常
2.用户取消
3.所有者取消
    */
    private Byte status;
}