package com.moesome.spike.model.vo;

import com.moesome.spike.exception.message.Code;
import com.moesome.spike.exception.message.ErrorCode;
import com.moesome.spike.model.domain.User;
import lombok.Data;

@Data
public class AuthResult {
	private int code;
	private String message;
	private Long timestamp;
	private User user;

	public static AuthResult AUTU_FAILED = new AuthResult(ErrorCode.UNAUTHORIZED,null);

	public AuthResult(Code code) {
		this(code,null);
	}

	public AuthResult(Code code, User user) {
		this.code = code.getCode();
		this.message = code.getMessage();
		this.timestamp = System.currentTimeMillis();
		this.user = user;
	}
}
