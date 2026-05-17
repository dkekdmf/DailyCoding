package com.example.zero.healthcare.exception.journal;

import com.example.zero.healthcare.exception.common.AbstractException;
import com.example.zero.healthcare.exception.common.ErrorCode;

public class PostAlreadyRecordedException extends AbstractException {

    public PostAlreadyRecordedException() {
        super(ErrorCode.POST_ALREADY_RECORDED);
    }
}
