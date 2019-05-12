package com.moesome.spike.exception.message;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ExceptionMsg {

    PASSWORD_MISMATCH(400,"用户名或密码错误"),
    WRONG_FORMAT(401,"用户名或密码格式错误"),
    ;
    private int code;
    private String msg;
}
