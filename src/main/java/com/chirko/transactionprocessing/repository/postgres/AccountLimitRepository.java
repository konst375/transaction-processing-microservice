package com.chirko.transactionprocessing.repository.postgres;

import com.chirko.transactionprocessing.model.postgres.AccountLimit;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountLimitRepository extends ListCrudRepository<AccountLimit, Integer> {
}
