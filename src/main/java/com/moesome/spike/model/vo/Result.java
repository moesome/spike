package com.moesome.spike.model.vo;

import com.moesome.spike.exception.message.Code;
import lombok.Data;

@Data
public class Result<T> {
	private int code;
	private String message;
	private Long timestamp;
	private T object;

	public Result(Code code) {
		this(code,null);
	}

	public Result(Code code, T object) {
		this.code = code.getCode();
		this.message = code.getMessage();
		this.timestamp = System.currentTimeMillis();
		this.object = object;
	}
}
