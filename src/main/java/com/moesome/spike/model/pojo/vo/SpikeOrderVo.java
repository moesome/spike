package com.moesome.spike.model.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpikeOrderVo {

	private Long userId;
	@NotNull
	private Long spikeId;
}
