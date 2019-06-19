package com.moesome.spike.model.pojo.result;


import com.moesome.spike.exception.message.Code;
import com.moesome.spike.exception.message.ErrorCode;

public class RechargeResult extends Result{
	public static final RechargeResult CARD_EXPIRED = new RechargeResult(ErrorCode.TIME_LIMIT_EXCEED);

	public RechargeResult(Code code) {
		super(code);
	}
}
