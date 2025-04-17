package com.chirko.transactionprocessing.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;

public record CompletedTransactionDto(
        int id,
        int accountFrom,
        int accountTo,
        String currencyShortname,
        BigDecimal sum,
        String expenseCategory,
        Timestamp datetime,
        BigDecimal limitSum,
        Timestamp limitDatetime,
        String limitCurrencyShortname
) {
}
