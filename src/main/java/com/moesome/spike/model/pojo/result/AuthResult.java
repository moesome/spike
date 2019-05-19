package com.moesome.spike.model.pojo.result;

import com.moesome.spike.exception.message.Code;
import com.moesome.spike.exception.message.ErrorCode;
import com.moesome.spike.model.domain.User;


public class AuthResult extends Result<User> {

	// 用于登录错误
	public static final AuthResult AUTH_FAILED = new AuthResult(ErrorCode.UNAUTHORIZED,null);
	public static final AuthResult USERNAME_OR_PASSWORD_ERR = new AuthResult(ErrorCode.USERNAME_OR_PASSWORD_ERR);
	// 用于传入的用户没有权限操作
	public static final AuthResult UNAUTHORIZED = new AuthResult(ErrorCode.UNAUTHORIZED);


	public AuthResult(Code code) {
		super(code);
	}

	public AuthResult(Code code, User user) {
		super(code, user);
	}
}
