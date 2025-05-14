package com.chirko.transactionprocessing.repository.postgres;

import com.chirko.transactionprocessing.model.postgres.ExchangeRateServiceMetadata;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface ExchangeRateServiceMetadataRepository extends CrudRepository<ExchangeRateServiceMetadata, LocalDate> {
}
