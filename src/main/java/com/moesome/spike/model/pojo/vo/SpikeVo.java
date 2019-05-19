package com.moesome.spike.model.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
	private Date startAt;

	/**
	 * 结束时间
	 */
	@NotNull
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
	private Date endAt;

	@Min(0)
	private Integer stock;
}
