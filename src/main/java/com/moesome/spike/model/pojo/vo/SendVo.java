package com.moesome.spike.model.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendVo {
	// spike
	private Long spikeId;
	private String name;
	// spike_order
	private Long spikeOrderId;

	private Date createdAt;

	private Byte status;

	// user
	private Long sendToUserId;

	private String username;

	private String email;

	private String phone;
}
