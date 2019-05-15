package com.moesome.spike.exception.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum  SuccessCode implements Code{
	OK(0,"成功"),
	;

	private int code;
	private String message;

}
