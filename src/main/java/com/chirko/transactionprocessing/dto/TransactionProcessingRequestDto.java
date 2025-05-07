package com.chirko.transactionprocessing.dto;

import com.chirko.transactionprocessing.dto.validation.constraints.AcceptableCategory;
import com.chirko.transactionprocessing.dto.validation.constraints.AcceptableCurrency;
import com.chirko.transactionprocessing.dto.validation.constraints.ValidDatetimeFormat;
import com.chirko.transactionprocessing.utils.CustomOffsetDateTimeDeserializer;
import com.chirko.transactionprocessing.utils.DatetimePair;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record TransactionProcessingRequestDto(
        @JsonProperty("account_from")
        int accountFrom,

        @JsonProperty("account_to")
        int accountTo,

        @JsonProperty("currency_shortname")
        @AcceptableCurrency
        String currencyShortname,

        @Positive(message = "Sum must be grater than zero, provided value: ${validatedValue}")
        BigDecimal sum,

        @JsonProperty("expense_category")
        @AcceptableCategory
        String expenseCategory,

        @JsonProperty("datetime")
        @JsonDeserialize(using = CustomOffsetDateTimeDeserializer.class)
        @ValidDatetimeFormat
        DatetimePair datetimePair
) {
}