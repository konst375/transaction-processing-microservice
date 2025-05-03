package com.chirko.transactionprocessing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TransactionDto(

        int id,

        @JsonProperty("account_from")
        int accountFrom,

        @JsonProperty("account_to")
        int accountTo,

        @JsonProperty("currency_shortname")
        String currencyShortname,

        BigDecimal sum,

        @JsonProperty("expense_category")
        String expenseCategory,

        OffsetDateTime datetime,

        @JsonProperty("limit_sum")
        BigDecimal limitSum,

        @JsonProperty("limit_datetime")
        OffsetDateTime limitDatetime,

        @JsonProperty("limit_currency_shortname")
        String limitCurrencyShortname
) {
}
