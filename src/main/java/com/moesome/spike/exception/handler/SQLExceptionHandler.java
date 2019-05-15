package com.moesome.spike.exception.handler;

import com.moesome.spike.exception.message.ErrorCode;
import com.moesome.spike.model.vo.ExceptionResult;
import com.moesome.spike.model.vo.OrderResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.SQLIntegrityConstraintViolationException;

//SQLIntegrityConstraintViolationException
@ControllerAdvice
@ResponseBody
public class SQLExceptionHandler {
	@ExceptionHandler(value = SQLIntegrityConstraintViolationException.class)
	public ResponseEntity<OrderResult> methodArgumentNotValidExceptionHandler(){
		return ResponseEntity.status(HttpStatus.OK).body(new OrderResult(ErrorCode.USER_DUPLICATE));
	}
}
