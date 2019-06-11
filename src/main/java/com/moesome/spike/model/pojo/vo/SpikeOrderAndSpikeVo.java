package com.moesome.spike.model.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpikeOrderAndSpikeVo {
	private Long spikeOrderId;
	private Long spikeId;
	private String spikeName;
	// spike 提供者 id
	private Long userId;
	private String userName;
	private String detail;
	private Date spikeOrderCreatedAt;
	private Byte status;
}
