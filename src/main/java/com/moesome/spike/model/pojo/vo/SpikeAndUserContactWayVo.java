package com.moesome.spike.model.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 存放 spike 创建者联系方式以及 spike 信息
 */
@Data
public class SpikeAndUserContactWayVo {
	// 所有者联系方式
	private String phone;
	private String email;
	// spike
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
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
	private Date startAt;

	/**
	 * 结束时间
	 */
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
	private Date endAt;

	private Integer stock;

	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
	private Date createdAt;

	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
	private Date updatedAt;
}
