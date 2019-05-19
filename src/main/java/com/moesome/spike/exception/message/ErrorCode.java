package com.moesome.spike.exception.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Random;

@AllArgsConstructor
@Getter
public enum ErrorCode implements Code{
	REQUEST_ERR(-400,"请求错误"),
	UNAUTHORIZED(-401,"未认证"),
	UNKNOWN_ERR(-402,"未知错误"),
	USERNAME_OR_PASSWORD_ERR(-629,"用户名或密码错误"),
	USER_DUPLICATE(-652,"重复的用户"),
	REPEATED_REQUEST(-508,"重复的请求"),
	LIMIT_EXCEED(-509,"超出数量限制"),
	TIME_LIMIT_EXCEED(-510,"超过结束时间"),
	TIME_LIMIT_NOT_ARRIVED(-511,"未到达开始时间"),
	IN_QUEUE(-512,"请求在队列中"),
	FAILED(-513,"请求失败"),
	;

	private int code;
	private String message;
}
