package com.chirko.transactionprocessing.repository.cassandra;

import com.chirko.transactionprocessing.model.cassandra.ExchangeRate;
import com.chirko.transactionprocessing.model.cassandra.ExchangeRateId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExchangeRateRepository extends CrudRepository<ExchangeRate, ExchangeRateId> {
}
