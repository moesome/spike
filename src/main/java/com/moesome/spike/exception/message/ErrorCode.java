package com.moesome.spike.exception.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode implements Code{
	REQUEST_ERR(-400,"请求错误"),
	UNAUTHORIZED(-401,"未认证"),
	USERNAME_OR_PASSWORD_ERR(-629,"用户名或密码错误"),
	USER_DUPLICATE(-652,"重复的用户");
	;

	private int code;
	private String message;
}
