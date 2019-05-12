package com.moesome.spike.model.vo;

import com.moesome.spike.model.po.User;
import lombok.Data;

@Data
public class LoginResult {
	private int status;
	private Long timestamp;
	private User user;

	public LoginResult(int status, User user) {
		this.status = status;
		this.timestamp = System.currentTimeMillis();
		this.user = user;
	}
}
