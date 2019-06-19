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
	// 用于在订单处理失败回滚时候给缓存中用户的金币回滚
	private String sessionId;
}
