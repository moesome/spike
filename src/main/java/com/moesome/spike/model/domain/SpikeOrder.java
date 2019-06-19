package com.moesome.spike.model.domain;

import java.math.BigDecimal;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpikeOrder implements Order {
	private Long id;

	/**
	 * 消费者 id
	 */
	private Long userId;

	private Long spikeId;

	private Date createdAt;

	/**
	 * 1.待发货
	 * 2.用户催单
	 * 3.所有者已发送奖品
	 * 4.完成订单
	 * 5.订单异常
	 */
	private Byte status;

	private BigDecimal price;
}