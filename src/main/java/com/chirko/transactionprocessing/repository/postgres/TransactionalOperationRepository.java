package com.chirko.transactionprocessing.repository.postgres;

import com.chirko.transactionprocessing.model.postgres.Account;
import com.chirko.transactionprocessing.model.postgres.TransactionalOperation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionalOperationRepository extends ListCrudRepository<TransactionalOperation, Integer> {


    @Query(value = """
            SELECT t.*
                FROM transactional_operation t
                WHERE t.account_from = :accountFromId
                  AND t.expense_category = :category
                  AND EXTRACT(YEAR FROM t.datetime) = EXTRACT(YEAR FROM CAST(:datetime AS TIMESTAMP))
                  AND EXTRACT(MONTH FROM t.datetime) = EXTRACT(MONTH FROM CAST(:datetime AS TIMESTAMP))
                ORDER BY t.datetime DESC
                LIMIT 1
            """, nativeQuery = true)
    Optional<TransactionalOperation> findLastThisMonthInCategory(int accountFromId,
                                                                 String category,
                                                                 OffsetDateTime datetime);

    @Query("""
            SELECT t
            FROM TransactionalOperation t
            WHERE t.id = :transactionId
            """)
    Optional<TransactionalOperation> findByTransactionId(int transactionId);

    /**
     * @param accountFromId Account id from which payment has been charged
     * @return A list of joined completed transaction and the account limit exceeded by the corresponding transaction,
     * in descending order.
     */
    @Query("""
            SELECT t
            FROM TransactionalOperation t
            WHERE t.accountFrom = :accountFrom
                AND t.limitExceeded = true
            ORDER BY t.datetime DESC
            """)
    List<TransactionalOperation> findByAccountFromAndLimitExceededTrue(Account accountFrom);
}
