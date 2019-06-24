package com.moesome.spike.model.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SpikePriceAndStockVo {
	private BigDecimal price;
	private Integer stock;
}
