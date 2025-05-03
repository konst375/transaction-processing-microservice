package com.chirko.transactionprocessing.service;

import com.chirko.transactionprocessing.dto.TransactionDto;
import com.chirko.transactionprocessing.dto.TransactionProcessingRequestDto;
import com.chirko.transactionprocessing.dto.mapper.TransactionalOperationMapper;
import com.chirko.transactionprocessing.exception.ErrorCode;
import com.chirko.transactionprocessing.exception.TransactionProcessingException;
import com.chirko.transactionprocessing.model.emuns.CurrencyShortname;
import com.chirko.transactionprocessing.model.emuns.ExpenseCategory;
import com.chirko.transactionprocessing.model.postgres.Account;
import com.chirko.transactionprocessing.model.postgres.AccountLimit;
import com.chirko.transactionprocessing.model.postgres.TransactionalOperation;
import com.chirko.transactionprocessing.repository.postgres.TransactionalOperationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionalOperationService {

    @Value("${transaction.processing.base.currency}")
    private CurrencyShortname defaultCurrency;

    private final TransactionalOperationRepository transactionalOperationRepository;

    private final ExchangeRateService exchangeRateService;

    private final AccountService accountService;

    private final TransactionalOperationMapper mapper = TransactionalOperationMapper.INSTANCE;

    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = TransactionProcessingException.class)
    public TransactionDto performTransaction(final TransactionProcessingRequestDto requestDto)
            throws TransactionProcessingException {

        // Obtaining Accounts to perform transaction
        final Account accountFrom = accountService.findById(requestDto.accountFrom());
        final Account accountTo = accountService.findById(requestDto.accountTo());

        // Counting sum in the target currency
        final CurrencyShortname paymentCurrency = CurrencyShortname.valueOf(requestDto.currencyShortname());
        final BigDecimal paymentRate = exchangeRateService.getExchangeRate(paymentCurrency, defaultCurrency);
        final BigDecimal targetSum = requestDto.sum().multiply(paymentRate);

        // Obtaining account payment info and counting balance in default currency
        final BigDecimal balanceFrom = accountFrom.getBalance();
        final CurrencyShortname accountCurrency = accountFrom.getCurrencyShortname();
        final BigDecimal balanceRate = exchangeRateService.getExchangeRate(accountCurrency, defaultCurrency);
        final BigDecimal balanceFromInTargetCurrency = accountCurrency.equals(defaultCurrency)
                ? balanceFrom
                : balanceFrom.multiply(balanceRate);

        // Parse custom timestamp format
        final OffsetDateTime datetime = requestDto.datetimePair().datetime();


        // Checking if there are enough funds
        if (balanceFromInTargetCurrency.compareTo(targetSum) < 0) {
            throw new TransactionProcessingException(ErrorCode.INSUFFICIENT_FUNDS,
                    requestDto.accountFrom(), OffsetDateTime.now(), requestDto.sum());
        }

        // Processing money transfer
        final BigDecimal updatedBalanceFrom = balanceFrom.subtract(targetSum).setScale(2, RoundingMode.HALF_EVEN);
        final BigDecimal updatedBalanceTo = accountTo.getBalance().add(targetSum).setScale(2, RoundingMode.HALF_EVEN);
        accountService.updateAccountsBalances(accountFrom, updatedBalanceFrom, accountTo, updatedBalanceTo);

        final ExpenseCategory expenseCategory = ExpenseCategory.valueOf(requestDto.expenseCategory().toUpperCase());

        // Checking if limit exceeded
        final LimitExceededCheckResult limitExceededCheckResult =
                checkIfLimitExceeded(expenseCategory, datetime, accountFrom, targetSum);
        final AccountLimit limit = limitExceededCheckResult.categoryLimit();

        final TransactionalOperation transactionalOperationToPersist = TransactionalOperation.builder()
                .currencyShortname(paymentCurrency)
                .datetime(datetime)
                .accountFrom(accountFrom)
                .accountTo(accountTo)
                .expenseCategory(expenseCategory)
                .sum(targetSum)
                .limitExceeded(limitExceededCheckResult.limitExceeded())
                .remainingMonthlyLimit(limitExceededCheckResult.remainingLimitAfterPayment())
                .limit(limit)
                .build();

        final TransactionalOperation savedTransaction = transactionalOperationRepository.save(transactionalOperationToPersist);

        return mapper.completedTransactionDtoFrom(savedTransaction);
    }

    private LimitExceededCheckResult checkIfLimitExceeded(ExpenseCategory expenseCategory, OffsetDateTime offsetDateTime,
                                                          Account accountFrom, BigDecimal targetSum) {

        // Looking for last transaction in category
        final Optional<TransactionalOperation> lastTransactionInCategory = transactionalOperationRepository
                .findLastThisMonthInCategory(accountFrom.getId(), expenseCategory.name(), offsetDateTime);

        final AccountLimit categoryLimit = accountFrom.getRemainingLimitForCategory(expenseCategory);

        final BigDecimal remainingInLimit = lastTransactionInCategory.isPresent()
                ? lastTransactionInCategory.get().getRemainingMonthlyLimit()
                : categoryLimit.getSum();

        // Calculating how much funds will remain
        final BigDecimal remainingLimitAfterPayment = remainingInLimit.subtract(targetSum);

        // Calculating if limit exceeded
        final boolean limitExceeded = remainingInLimit.compareTo(BigDecimal.ZERO) >= 0
                                      && remainingLimitAfterPayment.compareTo(BigDecimal.ZERO) < 0;

        return new LimitExceededCheckResult(categoryLimit, remainingLimitAfterPayment, limitExceeded);
    }

    public TransactionDto getByTransactionId(int id) throws TransactionProcessingException {
        final TransactionalOperation transaction = transactionalOperationRepository.findByTransactionId(id)
                .orElseThrow(() -> new TransactionProcessingException(ErrorCode.TRANSACTION_DOES_NOT_EXIST_FOR_ID, id));
        return mapper.completedTransactionDtoFrom(transaction);
    }

    public List<TransactionDto> getExceededLimitForAccount(int accountId) throws TransactionProcessingException {
        Account account = accountService.findById(accountId);
        return transactionalOperationRepository.findByAccountFromAndLimitExceededTrue(account)
                .stream()
                .map(mapper::completedTransactionDtoFrom)
                .toList();
    }

    private record LimitExceededCheckResult(
            AccountLimit categoryLimit,
            BigDecimal remainingLimitAfterPayment,
            boolean limitExceeded
    ) {
    }
}
