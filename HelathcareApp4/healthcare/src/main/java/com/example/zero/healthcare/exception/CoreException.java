package com.example.zero.healthcare.exception;

import com.example.zero.healthcare.exception.common.AbstractException;
import com.example.zero.healthcare.exception.common.ErrorCode;

public class CoreException extends AbstractException {

    public CoreException(ErrorCode errorCode) {
        super(errorCode);
    }
}
