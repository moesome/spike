package com.moesome.spike.exception.message;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
public enum ErrorCode implements Code{
	REQUEST_ERR(-400,"请求错误"),
	UNAUTHORIZED(-401,"未认证"),
	USERNAME_OR_PASSWORD_ERR(-629,"用户名或密码错误"),
	;

	private int code;
	private String message;
}
