package com.chirko.transactionprocessing.exception;

import lombok.Getter;

@Getter
public class TransactionProcessingException extends Exception {

    private ErrorCode errorCode;

    public TransactionProcessingException(String formattedMessage) {
        super(formattedMessage);
    }

    public TransactionProcessingException(ErrorCode errorCode, Object... args) {
        this(String.format(errorCode.getFormat(), args));
        this.errorCode = errorCode;
    }
}
