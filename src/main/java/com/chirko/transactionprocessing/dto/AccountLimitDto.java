package com.chirko.transactionprocessing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record AccountLimitDto(

        int account,

        @JsonProperty("expense_category")
        String expenseCategory,

        @JsonProperty("currency_shortname")
        String currencyShortname,

        OffsetDateTime datetime,

        BigDecimal sum
) {
}
