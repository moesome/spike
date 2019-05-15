package com.moesome.spike.model.vo;

import com.moesome.spike.exception.message.Code;
import com.moesome.spike.exception.message.ErrorCode;
import com.moesome.spike.model.domain.Order;

public class OrderResult extends Result<Order> {

	public static final OrderResult REQUEST_ERR = new OrderResult(ErrorCode.REQUEST_ERR);
	public static final OrderResult UNAUTHORIZED = new OrderResult(ErrorCode.UNAUTHORIZED);



	public OrderResult(Code code) {
		super(code);
	}

	public OrderResult(Code code, Order order) {
		super(code, order);
	}
}
