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
        final BigDecimal paymentSum = requestDto.sum();
        final BigDecimal paymentRate = exchangeRateService.getExchangeRate(paymentCurrency, defaultCurrency);
        final BigDecimal paymentSumInDefaultCurrency = paymentSum.multiply(paymentRate);

        // Processing payment
        final BigDecimal accountFromBalance = accountFrom.getBalance();
        final CurrencyShortname accountFromCurrency = accountFrom.getCurrencyShortname();

        BigDecimal updatedBalanceFrom;
        if (accountFromCurrency.equals(paymentCurrency)) {
            if (accountFromBalance.compareTo(paymentSum) < 0) {
                throw new TransactionProcessingException(ErrorCode.INSUFFICIENT_FUNDS,
                        requestDto.accountFrom(), OffsetDateTime.now(), paymentSum);
            } else {
                updatedBalanceFrom = accountFromBalance.subtract(paymentSum);
            }
        } else {
            final BigDecimal balanceFromRate = exchangeRateService.getExchangeRate(accountFromCurrency, defaultCurrency);
            final BigDecimal paymentSumInAccountFromCurrency = paymentSumInDefaultCurrency.divide(balanceFromRate,
                    RoundingMode.HALF_EVEN);

            if (accountFromBalance.compareTo(paymentSumInAccountFromCurrency) < 0) {
                throw new TransactionProcessingException(ErrorCode.INSUFFICIENT_FUNDS,
                        requestDto.accountFrom(), OffsetDateTime.now(), paymentSum);
            } else {
                updatedBalanceFrom = accountFromBalance.subtract(paymentSumInAccountFromCurrency);
            }
        }

        final CurrencyShortname accountToCurrency = accountTo.getCurrencyShortname();

        BigDecimal updatedBalanceTo;
        if (accountToCurrency.equals(paymentCurrency)) {
            updatedBalanceTo = accountTo.getBalance()
                    .add(paymentSum)
                    .setScale(2, RoundingMode.HALF_EVEN);
        } else {
            final BigDecimal paymentSumInAccountToCurrency = paymentSumInDefaultCurrency.divide(
                    exchangeRateService.getExchangeRate(accountToCurrency, defaultCurrency), RoundingMode.HALF_EVEN);
            updatedBalanceTo = accountTo.getBalance().add(paymentSumInAccountToCurrency);
        }

        accountService.updateAccountsBalances(accountFrom, updatedBalanceFrom, accountTo, updatedBalanceTo);

        // Parse custom timestamp format
        final OffsetDateTime datetime = requestDto.datetimePair().datetime();

        // Checking if limit exceeded
        final ExpenseCategory expenseCategory = ExpenseCategory.valueOf(requestDto.expenseCategory().toUpperCase());
        final LimitExceededCheckResult limitExceededCheckResult =
                checkIfLimitExceeded(expenseCategory, datetime, accountFrom, paymentSumInDefaultCurrency);
        final AccountLimit limit = limitExceededCheckResult.limit();

        final TransactionalOperation transactionalOperationToPersist = TransactionalOperation.builder()
                .currencyShortname(paymentCurrency)
                .datetime(datetime)
                .accountFrom(accountFrom)
                .accountTo(accountTo)
                .expenseCategory(expenseCategory)
                .sum(paymentSumInDefaultCurrency)
                .limitExceeded(limitExceededCheckResult.limitExceeded())
                .remainingMonthlyLimit(limitExceededCheckResult.remains())
                .limit(limit)
                .build();

        final TransactionalOperation savedTransaction = transactionalOperationRepository.save(transactionalOperationToPersist);

        return mapper.completedTransactionDtoFrom(savedTransaction);
    }

    LimitExceededCheckResult checkIfLimitExceeded(ExpenseCategory expenseCategory, OffsetDateTime datetime,
                                                  Account accountFrom, BigDecimal targetSum) {

        // Looking for last transaction in category
        final Optional<TransactionalOperation> lastTransactionInCategory = transactionalOperationRepository
                .findLastThisMonthInCategory(accountFrom.getId(), expenseCategory.name(), datetime);

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

    record LimitExceededCheckResult(
            AccountLimit limit,
            BigDecimal remains,
            boolean limitExceeded
    ) {
    }
}
