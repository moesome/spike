package com.moesome.spike.model.vo;

import com.moesome.spike.exception.message.Code;
import lombok.Data;

@Data
public class ExceptionResult extends Result {
    public ExceptionResult(Code code) {
        super(code);
    }
}
