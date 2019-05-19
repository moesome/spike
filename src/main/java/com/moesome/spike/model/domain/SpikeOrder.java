package com.moesome.spike.model.domain;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpikeOrder implements Order{
    private Long id;
    @NotNull
    private Long userId;
    @NotNull
    private Long spikeId;

    private Date createdAt;

    /**
    * 1.正常
2.用户取消
3.所有者取消
    */
    private Byte status;
}