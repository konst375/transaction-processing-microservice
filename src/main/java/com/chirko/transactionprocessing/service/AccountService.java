package com.chirko.transactionprocessing.service;

import com.chirko.transactionprocessing.dto.AccountDto;
import com.chirko.transactionprocessing.dto.AccountLimitDto;
import com.chirko.transactionprocessing.dto.CreateAccountDto;
import com.chirko.transactionprocessing.dto.UpdateSumRequestDto;
import com.chirko.transactionprocessing.dto.mapper.AccountLimitMapper;
import com.chirko.transactionprocessing.dto.mapper.AccountMapper;
import com.chirko.transactionprocessing.exception.ErrorCode;
import com.chirko.transactionprocessing.exception.TransactionProcessingException;
import com.chirko.transactionprocessing.model.emuns.CurrencyShortname;
import com.chirko.transactionprocessing.model.emuns.ExpenseCategory;
import com.chirko.transactionprocessing.model.postgres.Account;
import com.chirko.transactionprocessing.model.postgres.AccountLimit;
import com.chirko.transactionprocessing.repository.postgres.AccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    private final AccountLimitService accountLimitService;

    private final AccountLimitMapper accountLimitMapper = AccountLimitMapper.INSTANCE;

    private final AccountMapper accountMapper = AccountMapper.INSTANCE;

    public Account findById(int id) throws TransactionProcessingException {
        return accountRepository.findById(id)
                .orElseThrow(() -> new TransactionProcessingException(ErrorCode.ACCOUNT_DOES_NOT_EXIST_FOR_ID, id));
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = TransactionProcessingException.class)
    public void updateAccountsBalances(Account accountFrom, BigDecimal updatedBalanceFrom,
                                       Account accountTo, BigDecimal updatedBalanceTo) {
        accountFrom.setBalance(updatedBalanceFrom);
        accountTo.setBalance(updatedBalanceTo);

        accountRepository.save(accountFrom);
        accountRepository.save(accountTo);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = TransactionProcessingException.class)
    public AccountLimitDto setNewAccountLimit(int accountId, ExpenseCategory category, UpdateSumRequestDto requestDto)
            throws TransactionProcessingException {
        final Account account = findById(accountId);

        final AccountLimit savedLimit = accountLimitService.buildAndSaveLimit(requestDto.sum(), category, account);
        account.getAccountLimits().put(category, savedLimit);

        return accountLimitMapper.accountLimitDtoFrom(savedLimit);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = TransactionProcessingException.class, readOnly = true)
    public Map<ExpenseCategory, AccountLimitDto> getAccountLimits(int accountId) throws TransactionProcessingException {
        final Account account = findById(accountId);
        final Map<ExpenseCategory, AccountLimit> accountLimits = account.getAccountLimits();
        final Map<ExpenseCategory, AccountLimitDto> result = new HashMap<>();

        accountLimits.forEach((category, value) -> {
            AccountLimitDto limitDto = accountLimitMapper.accountLimitDtoFrom(value);
            result.put(category, limitDto);
        });
        return result;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public AccountDto createAccount(CreateAccountDto requestDto) {
        final Account accountToPersist = Account.builder()
                .currencyShortname(CurrencyShortname.valueOf(requestDto.currencyShortname()))
                .balance(requestDto.balance())
                .build();

        final Account account = accountRepository.save(accountToPersist);
        final Map<ExpenseCategory, AccountLimit> limits = accountLimitService.createLimitsForAccount(account);

        account.setAccountLimits(limits);
        final Account saved = accountRepository.save(account);

        return accountMapper.accountDtoFrom(saved);
    }

    public AccountDto getAccount(int accountId) throws TransactionProcessingException {
        final Account account = findById(accountId);
        return accountMapper.accountDtoFrom(account);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = TransactionProcessingException.class)
    public AccountDto updateAccountBalance(int accountId, UpdateSumRequestDto requestDto)
            throws TransactionProcessingException {
        Account account = findById(accountId);
        account.setBalance(requestDto.sum());
        return accountMapper.accountDtoFrom(account);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, readOnly = true)
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }
}
