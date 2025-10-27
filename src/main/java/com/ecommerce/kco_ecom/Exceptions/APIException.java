package com.ecommerce.kco_ecom.Exceptions;

public class APIException extends RuntimeException {
    public APIException() {
        super();
    }

    public APIException(String message) {
        super(message);
    }

    public APIException(String message, Throwable cause) {
        super(message, cause);
    }
}

