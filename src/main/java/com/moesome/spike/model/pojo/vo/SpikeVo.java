package com.moesome.spike.model.pojo.vo;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class SpikeVo {
	@NotNull
	@Size(max = 32,min = 1)
	private String name;

	@NotNull
	private String detail;

	/**
	 * 起始时间
	 */
	@NotNull
	private Date startAt;

	/**
	 * 结束时间
	 */
	@NotNull
	private Date endAt;

	@Min(0)
	private Integer stock;

	@NotNull
	@Min(0)
	@Max(1000000)
	private BigDecimal price;
}
