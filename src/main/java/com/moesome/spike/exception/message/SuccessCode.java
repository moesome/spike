package com.moesome.spike.exception.message;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
public enum  SuccessCode implements Code{
	OK(0,"成功"),
	;

	private int code;
	private String message;

}
