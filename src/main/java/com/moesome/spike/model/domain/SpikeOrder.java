package com.moesome.spike.model.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpikeOrder implements Order{
    private Long id;

    private Long userId;

    private Long spikeId;

    private Date createdAt;

    /**
    * 1.正常
2.用户取消
3.所有者取消
    */
    private Byte status;
}