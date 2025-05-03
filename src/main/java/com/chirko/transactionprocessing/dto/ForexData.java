package com.chirko.transactionprocessing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.Map;

public record ForexData(
        @JsonProperty("Meta Data")
        ForexMetaData metaData,

        @JsonProperty("Time Series FX (Daily)")
        Map<LocalDate, DailyData> timeSeries
) {
}
