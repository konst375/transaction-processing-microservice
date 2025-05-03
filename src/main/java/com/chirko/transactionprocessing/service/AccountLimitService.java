package com.chirko.transactionprocessing.service;

import com.chirko.transactionprocessing.exception.TransactionProcessingException;
import com.chirko.transactionprocessing.model.emuns.CurrencyShortname;
import com.chirko.transactionprocessing.model.emuns.ExpenseCategory;
import com.chirko.transactionprocessing.model.postgres.Account;
import com.chirko.transactionprocessing.model.postgres.AccountLimit;
import com.chirko.transactionprocessing.repository.postgres.AccountLimitRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountLimitService {

    private final Logger logger = LoggerFactory.getLogger(AccountLimitService.class);

    private final AccountLimitRepository accountLimitRepository;

    private AccountService accountService;

    @Lazy
    @Autowired
    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = TransactionProcessingException.class)
    public AccountLimit buildAndSaveLimit(BigDecimal sum, ExpenseCategory category, Account account) {
        final AccountLimit limitToPersist = AccountLimit.builder()
                .account(account)
                .sum(sum)
                .expenseCategory(category)
                .currencyShortname(CurrencyShortname.USD)
                .datetime(OffsetDateTime.now())
                .build();
        return accountLimitRepository.save(limitToPersist);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = TransactionProcessingException.class)
    public Map<ExpenseCategory, AccountLimit> createLimitsForAccount(Account account) {
        final List<AccountLimit> limitsToPersist = buildLimitsForAccounts(List.of(account));
        final List<AccountLimit> saved = accountLimitRepository.saveAll(limitsToPersist);

        return saved.stream().collect(Collectors.toMap(
                AccountLimit::getExpenseCategory,
                limit -> limit
        ));
    }

    @Scheduled(cron = "@monthly")
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    protected void updateAccountsLimits() {
        logger.info("Starting monthly update limits for each Account");
        final Instant start = Instant.now();

        final List<Account> accounts = accountService.getAllAccounts();

        final double blockingFactor = 0.5;
        final int availableProcessors = Runtime.getRuntime().availableProcessors();

        try (ForkJoinPool forkJoinPool = new ForkJoinPool((int) (availableProcessors / (1 - blockingFactor)))) {
            forkJoinPool.execute(new RecursiveUpdateLimitsAction(accounts, accountLimitRepository));
        }
        final Instant end = Instant.now();
        logger.info("Limits updating Start time: {}, End time: {}, Elapsed time: {}",
                start, end, Duration.between(start, end).toString());
    }

    private static List<AccountLimit> buildLimitsForAccounts(List<Account> accounts) {
        final ExpenseCategory[] categories = ExpenseCategory.values();
        final List<AccountLimit> limits = new ArrayList<>(accounts.size() * categories.length);
        accounts.forEach(account -> {
            for (ExpenseCategory category : categories) {
                final AccountLimit limit = AccountLimit.builder()
                        .currencyShortname(CurrencyShortname.USD)
                        .account(account)
                        .expenseCategory(category)
                        .build();
                limits.add(limit);
            }
        });
        return limits;
    }

    private static class RecursiveUpdateLimitsAction extends RecursiveAction {

        private static final int THRESHOLD = 10;

        private final List<Account> accounts;

        private final AccountLimitRepository repository;

        private RecursiveUpdateLimitsAction(List<Account> accounts, AccountLimitRepository repository) {
            this.accounts = accounts;
            this.repository = repository;
        }

        @Override
        protected void compute() {
            if (accounts.size() > THRESHOLD) {
                ForkJoinTask.invokeAll(createSubtasks());
            } else {
                processing(accounts);
            }
        }

        private List<RecursiveUpdateLimitsAction> createSubtasks() {
            List<RecursiveUpdateLimitsAction> subtasks = new ArrayList<>();

            List<Account> partOne = accounts.subList(0, accounts.size() / 2);
            List<Account> partTwo = accounts.subList(accounts.size() / 2, accounts.size());

            subtasks.add(new RecursiveUpdateLimitsAction(partOne, repository));
            subtasks.add(new RecursiveUpdateLimitsAction(partTwo, repository));

            return subtasks;
        }

        private void processing(List<Account> accounts) {
            List<AccountLimit> limits = buildLimitsForAccounts(accounts);
            repository.saveAll(limits);
        }
    }

}
