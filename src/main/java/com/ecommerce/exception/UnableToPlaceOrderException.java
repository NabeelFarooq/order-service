package com.ecommerce.exception;

public class UnableToPlaceOrderException extends RuntimeException{

	public UnableToPlaceOrderException(String _message) {
		super(_message);
	}

}
