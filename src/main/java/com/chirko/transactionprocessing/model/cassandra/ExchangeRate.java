package com.chirko.transactionprocessing.model.cassandra;

import lombok.Builder;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.math.BigDecimal;

@Table("exchange_rate")
@Builder
public record ExchangeRate(

        @PrimaryKey
        ExchangeRateId exchangeRateId,

        BigDecimal rate
) {
}

