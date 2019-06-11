package com.moesome.spike.model.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpikeOrderVo {
	private Long userId;
	@NotNull
	private Long spikeId;
	private String sessionId;
}
