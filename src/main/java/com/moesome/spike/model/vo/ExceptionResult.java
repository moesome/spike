package com.moesome.spike.model.vo;

import com.moesome.spike.exception.message.ExceptionMsg;
import lombok.Data;

@Data
public class ExceptionResult {
    private int status;
    private String message;
    private Long timestamp;

    public ExceptionResult(ExceptionMsg exceptionMsg) {
        this(exceptionMsg.getCode(),exceptionMsg.getMsg());
    }

    public ExceptionResult(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }
}
