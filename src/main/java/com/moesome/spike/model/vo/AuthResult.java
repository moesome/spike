package com.moesome.spike.model.vo;

import com.moesome.spike.exception.message.Code;
import com.moesome.spike.exception.message.ErrorCode;
import com.moesome.spike.model.domain.User;


public class AuthResult extends Result<User> {

	public static final AuthResult AUTU_FAILED = new AuthResult(ErrorCode.UNAUTHORIZED,null);
	public static final AuthResult USERNAME_OR_PASSWORD_ERR = new AuthResult(ErrorCode.USERNAME_OR_PASSWORD_ERR);


	public AuthResult(Code code) {
		super(code);
	}

	public AuthResult(Code code, User user) {
		super(code, user);
	}
}
