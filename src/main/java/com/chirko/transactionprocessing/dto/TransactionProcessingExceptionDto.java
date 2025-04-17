package com.chirko.transactionprocessing.dto;

import org.springframework.http.HttpStatus;

import java.sql.Timestamp;

public record TransactionProcessingExceptionDto(
        Timestamp timestamp,
        int errorCode,
        String message,
        int status,
        HttpStatus error,
        String reason,
        String path
) {
}
