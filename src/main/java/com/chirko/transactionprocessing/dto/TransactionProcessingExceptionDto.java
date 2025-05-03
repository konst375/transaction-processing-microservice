package com.chirko.transactionprocessing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;

@Builder
public record TransactionProcessingExceptionDto(

        OffsetDateTime datetime,

        @JsonProperty("error_code")
        int errorCode,

        String message,

        @JsonProperty("status_code")
        int statusCode,

        @JsonProperty("http_status")
        HttpStatus httpStatus,

        String exception,

        String path
) {
}
