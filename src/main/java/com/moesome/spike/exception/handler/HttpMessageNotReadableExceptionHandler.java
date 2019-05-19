package com.moesome.spike.exception.handler;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@ResponseBody
public class HttpMessageNotReadableExceptionHandler {
	/*@ExceptionHandler(value = HttpMessageNotReadableException.class)
	public ResponseEntity<ExceptionResult> methodArgumentNotValidExceptionHandler(){
		return ResponseEntity.status(HttpStatus.OK).body(new ExceptionResult(ErrorCode.REQUEST_ERR));
	}*/
}