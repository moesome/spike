package com.moesome.spike.exception.handler;

import com.moesome.spike.exception.message.ErrorCode;
import com.moesome.spike.model.pojo.result.ExceptionResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@ResponseBody
public class CommonExceptionHandler {
	//@ExceptionHandler(value = Exception.class)
	public ResponseEntity<ExceptionResult> methodArgumentNotValidExceptionHandler(){
		return ResponseEntity.status(HttpStatus.OK).body(new ExceptionResult(ErrorCode.UNKNOWN_ERR));
	}
}