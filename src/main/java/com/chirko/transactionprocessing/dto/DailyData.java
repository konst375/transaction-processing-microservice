package com.chirko.transactionprocessing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record DailyData(
        @JsonProperty("1. open")
        BigDecimal open,

        @JsonProperty("2. high")
        BigDecimal high,

        @JsonProperty("3. low")
        BigDecimal low,

        @JsonProperty("4. close")
        BigDecimal close
) {
}
