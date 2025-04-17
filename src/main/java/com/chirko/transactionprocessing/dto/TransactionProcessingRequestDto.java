package com.chirko.transactionprocessing.dto;

import com.chirko.transactionprocessing.dto.validation.constraints.AcceptableCategory;
import com.chirko.transactionprocessing.dto.validation.constraints.AcceptableCurrency;
import com.chirko.transactionprocessing.dto.validation.constraints.ValidDate;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransactionProcessingRequestDto(
        @JsonProperty("account_from")
        int accountFrom,

        @JsonProperty("account_to")
        int accountTo,

        @JsonProperty("currency_shortname")
        @AcceptableCurrency(message = "Unacceptable currency: {currencyShortname}")
        String currencyShortname,

        @Positive
        BigDecimal sum,

        @JsonProperty("expense_category")
        @AcceptableCategory(message = "Unacceptable transaction category: {expenseCategory}")
        String expenseCategory,

//        @Pattern(regexp = "^(19|20)\\d{2}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01]) ([01][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])([+-])(0[0-9]|1[0-4])$",
//                message = "invalid datetime format, mast be in format like: 2022-01-30 00:00:00+06")
        @ValidDate(message = "invalid datetime format, mast be in format like: 2022-01-30 00:00:00+06")
        String datetime
) {
}