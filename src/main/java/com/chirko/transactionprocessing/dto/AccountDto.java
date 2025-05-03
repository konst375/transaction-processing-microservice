package com.chirko.transactionprocessing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

public record AccountDto(

        int id,

        @JsonProperty("currency_shortname")
        String currencyShortname,

        OffsetDateTime datetime,

        BigDecimal balance,

        @JsonProperty("account_limits")
        Map<String, AccountLimitDto> accountLimits,

        @JsonProperty("updated_datetime")
        OffsetDateTime updatedDatetime
) {
}
