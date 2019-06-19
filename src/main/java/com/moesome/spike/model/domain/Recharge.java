package com.moesome.spike.model.domain;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
public class Recharge {
	private Long id;

	private Long userId;

	private BigDecimal coin;

	private Date createdAt;

	/**
	 * -1：已取消
	 * 0：未支付
	 * 1：已支付
	 */
	private Byte status;

	private Date payAt;

	/**
	 * 如果是支付宝则为支付宝订单号，如果为充值卡则为充值卡号
	 */
	private String tradeNo;

	/**
	 * 1：支付宝
	 * 2：充值卡
	 */
	private Byte way;
}