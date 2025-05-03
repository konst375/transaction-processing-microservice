package com.chirko.transactionprocessing.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {

    /**
     * Account
     */
    ACCOUNT_DOES_NOT_EXIST_FOR_ID(HttpStatus.NOT_FOUND, 1001,"Account does not exist for id: %s"),
    ACCOUNT_UPDATE_FAILED(HttpStatus.CONFLICT, 1002,"Account update failed."),

    /**
     * Limit
     */
    LIMIT_DOES_NOT_EXIST_FOR_ID(HttpStatus.NOT_FOUND, 2001, "Limit does not exist for id: %s"),
    LIMIT_DOES_NOT_EXIST_FOR_ACCOUNT_AND_BEFORE_DATETIME(HttpStatus.NOT_FOUND, 2002, "Limit does not exist for account: %s, and before datetimePair: %s"),

    /**
     * Transaction
     */
    TRANSACTION_DOES_NOT_EXIST_FOR_ID(HttpStatus.NOT_FOUND, 3001,"Transaction does not exist for id: %s"),

    /**
     * Payment
     */
    PAYMENT_INVALID_PARAMETER(HttpStatus.BAD_REQUEST, 4001, "Invalid parameter %s: %s"),
    INSUFFICIENT_FUNDS(HttpStatus.PAYMENT_REQUIRED, 4002, "Insufficient funds to make the payment, accountId: %s, transaction datetimePair: %s, payment amount: %s"),
    UNSUPPORTED_CURRENCY(HttpStatus.UNPROCESSABLE_ENTITY, 4003, "Payment in %s is not available"),


    /**
     * Internal service errors
     */
    INTERNAL_SERVICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 5001, "Internal server error occurred: error: %s"),

    /**
     * Data validation
     */
    INVALID_DATA_SUBMITTED(HttpStatus.BAD_REQUEST, 6001, "Invalid data submitted, errors: ");


    private final HttpStatus httpStatus;
    private final int code;
    private final String format;
}
