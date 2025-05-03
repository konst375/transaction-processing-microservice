package com.chirko.transactionprocessing.repository.postgres;

import com.chirko.transactionprocessing.model.postgres.Account;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends ListCrudRepository<Account, Integer> {
}
