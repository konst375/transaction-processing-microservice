package com.chirko.transactionprocessing.dto;

import com.chirko.transactionprocessing.model.emuns.CurrencyShortname;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Date;

public record ForexMetaData(
        @JsonProperty("1. Information")
        String information,

        @JsonProperty("2. From Symbol")
        CurrencyShortname fromSymbol,

        @JsonProperty("3. To Symbol")
        CurrencyShortname toSymbol,

        @JsonProperty("4. Output Size")
        String outputSize,

        @JsonProperty("5. Last Refreshed")
        Date lastRefreshed,

        @JsonProperty("6. Time Zone")
        String timeZone
) {
}
