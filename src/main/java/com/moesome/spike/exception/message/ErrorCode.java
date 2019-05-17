package com.moesome.spike.exception.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Random;

@AllArgsConstructor
@Getter
public enum ErrorCode implements Code{
	REQUEST_ERR(-400,"请求错误"),
	UNAUTHORIZED(-401,"未认证"),
	USERNAME_OR_PASSWORD_ERR(-629,"用户名或密码错误"),
	USER_DUPLICATE(-652,"重复的用户"),
	CANT_ADD_TO_QUEUE(-508,"订单创建失败"),
	LIMIT_EXCEED(-509,"超出限制"),
	TIME_LIMIT_EXCEED(-510,"超过结束时间"),
	TIME_LIMIT_NOT_ARRIVED(-511,"未到达开始时间"),
	;

	private int code;
	private String message;
}
