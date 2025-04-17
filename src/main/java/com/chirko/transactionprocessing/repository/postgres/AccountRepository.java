package com.chirko.transactionprocessing.repository.postgres;

import com.chirko.transactionprocessing.model.postgres.Account;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends CrudRepository<Account, Integer> {
}
