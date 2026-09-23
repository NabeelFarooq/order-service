package com.ecommerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ecommerce.order.request.response.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(UnableToPlaceOrderException.class)
	public ResponseEntity<ErrorResponse> handle(UnableToPlaceOrderException e) {
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setErrorCode("ERR-05");
		errorResponse.setMessage(e.getMessage());
		e.printStackTrace();
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
	}
	
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handle(Exception e) {
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setErrorCode("ERR-03");
		errorResponse.setMessage(e.getMessage());
		e.printStackTrace();
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
	}
}
