package com.example.zero.healthcare.exception.journal;

import com.example.zero.healthcare.exception.common.AbstractException;
import com.example.zero.healthcare.exception.common.ErrorCode;

public class JournalNotFoundException extends AbstractException {

    public JournalNotFoundException() {
        super(ErrorCode.JOURNAL_NOT_FOUND);
    }
}
