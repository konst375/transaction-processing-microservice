package com.chirko.transactionprocessing.model.cassandra;

import com.chirko.transactionprocessing.model.emuns.CurrencyShortname;
import lombok.Builder;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.time.LocalDate;

@PrimaryKeyClass
@Builder
public record ExchangeRateId(

        @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED)
        CurrencyShortname base,

        @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.PARTITIONED)
        CurrencyShortname target,

        @PrimaryKeyColumn(ordinal = 2, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
        LocalDate date
) {
}
