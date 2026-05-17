package com.example.zero.healthcare.exception.common;

import lombok.Getter;

@Getter
public abstract class AbstractException extends RuntimeException {

    private final ErrorCode errorCode;

    public AbstractException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public int getStatus() { return errorCode.getStatus(); }

    public String getCode() { return errorCode.getCode(); }
}
