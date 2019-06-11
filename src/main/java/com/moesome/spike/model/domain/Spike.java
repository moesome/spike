package com.moesome.spike.model.domain;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
public class Spike {
	private Long id;

	private String name;

	/**
	 * 创建者
	 */
	private Long userId;

	private String detail;

	/**
	 * 起始时间
	 */
	private Date startAt;

	/**
	 * 结束时间
	 */
	private Date endAt;

	private Integer stock;

	private Date createdAt;

	private Date updatedAt;

	private BigDecimal price;
}