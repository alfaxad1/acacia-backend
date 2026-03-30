package com.example.acacia.Exception;

import lombok.Getter;

@Getter
public class ApplicationException extends RuntimeException {

    private final String errorCode;

    public ApplicationException(String message) {
        super(message);
        this.errorCode = "APPLICATION_ERROR";
    }

    public ApplicationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

}