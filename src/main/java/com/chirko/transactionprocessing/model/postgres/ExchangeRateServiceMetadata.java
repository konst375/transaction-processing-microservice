package com.chirko.transactionprocessing.model.postgres;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Table(name = "exchange_rate_service_metadata")
public final class ExchangeRateServiceMetadata {

    @Id
    @Column(name = "last_fetch_date")
    private LocalDate lastFetchDate;

}
