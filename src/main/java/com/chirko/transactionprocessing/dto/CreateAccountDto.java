package com.chirko.transactionprocessing.dto;

import com.chirko.transactionprocessing.dto.validation.constraints.AcceptableCurrency;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreateAccountDto(

        @JsonProperty("currency_shortname")
        @AcceptableCurrency(message = "Unacceptable currency: {currencyShortname}")
        String currencyShortname,

        @PositiveOrZero
        BigDecimal balance
) {
}
