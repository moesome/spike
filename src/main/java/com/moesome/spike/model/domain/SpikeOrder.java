package com.moesome.spike.model.domain;

import java.util.Date;
import lombok.Data;

@Data
public class SpikeOrder {
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