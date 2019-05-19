package com.moesome.spike.model.pojo.result;

import com.moesome.spike.exception.message.Code;
import com.moesome.spike.exception.message.ErrorCode;
import com.moesome.spike.exception.message.SuccessCode;
import com.moesome.spike.model.domain.User;

public class UserResult extends Result<User> {
	public static final UserResult OK_WITHOUT_BODY = new UserResult(SuccessCode.OK);
	public static final UserResult USER_DUPLICATE = new UserResult(ErrorCode.USER_DUPLICATE);
	public UserResult(Code code) {
		super(code);
	}

	public UserResult(Code code, User object) {
		super(code, object);
	}
}
