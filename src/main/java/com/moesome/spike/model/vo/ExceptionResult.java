package com.moesome.spike.model.vo;

import com.fasterxml.jackson.annotation.JsonValue;
import com.moesome.spike.exception.message.Code;
import com.moesome.spike.exception.message.ErrorCode;
import lombok.Data;

@Data
public class ExceptionResult {
    private int code;
    private String message;
    private Long timestamp;

    public ExceptionResult(Code code) {
        this.code = code.getCode();
        this.message = code.getMessage();
        this.timestamp = System.currentTimeMillis();
    }

}
