package com.moesome.spike.model.vo.receive;

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
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
	private Date spikeOrderCreatedAt;
	private Byte status;
}
