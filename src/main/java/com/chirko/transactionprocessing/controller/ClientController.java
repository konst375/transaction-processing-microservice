package com.chirko.transactionprocessing.controller;

import com.chirko.transactionprocessing.dto.AccountDto;
import com.chirko.transactionprocessing.dto.AccountLimitDto;
import com.chirko.transactionprocessing.dto.CreateAccountDto;
import com.chirko.transactionprocessing.dto.UpdateSumRequestDto;
import com.chirko.transactionprocessing.exception.TransactionProcessingException;
import com.chirko.transactionprocessing.model.emuns.ExpenseCategory;
import com.chirko.transactionprocessing.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/transaction-service/accounts")
@RequiredArgsConstructor
public class ClientController {

    private final AccountService accountService;

    /**
     * Creates new Account.
     * @param requestDto Submitted data.
     * @return {@link AccountDto}
     */
    @PostMapping
    public ResponseEntity<AccountDto> createNewAccount(@RequestBody @Valid CreateAccountDto requestDto) {
        final AccountDto response = accountService.createAccount(requestDto);

        final URI location = ServletUriComponentsBuilder
                .fromPath("/api/v1/transaction-service/accounts")
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    /**
     * Finds Account by id.
     * @param accountId Account id.
     * @return {@link AccountLimitDto}
     * @throws TransactionProcessingException If there is no account for provided {@code accountId}.
     */
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDto> getAccount(@PathVariable int accountId) throws TransactionProcessingException {
        final AccountDto response = accountService.getAccount(accountId);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates Account's balance.
     * @param accountId Account id whose balance will be updated.
     * @param requestDto Submitted data.
     * @return {@link AccountDto}
     * @throws TransactionProcessingException If there is no account for provided {@code accountId}.
     */
    @PatchMapping("/{accountId}/balance")
    public ResponseEntity<AccountDto> updateBalance(@PathVariable int accountId,
                                                    @RequestBody UpdateSumRequestDto requestDto)
            throws TransactionProcessingException {
        final AccountDto response = accountService.updateAccountBalance(accountId, requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * Creates new {@code AccountLimit} in a specific category for a specific account.
     * @param accountId  Account id for which limit will be updated
     * @param category   Category of limit (product / service)
     * @param requestDto Submitted data
     * @return {@link AccountLimitDto}
     * @throws TransactionProcessingException If there is no account for provided {@code accountId}.
     */
    @PostMapping("/{accountId}/limits")
    public ResponseEntity<AccountLimitDto> createNewAccountLimit(
            @PathVariable int accountId,
            @RequestParam String category,
            @RequestBody @Valid UpdateSumRequestDto requestDto) throws TransactionProcessingException {
        final ExpenseCategory expenseCategory = ExpenseCategory.valueOf(category.toUpperCase());
        final AccountLimitDto result = accountService.setNewAccountLimit(accountId, expenseCategory, requestDto);
        return ResponseEntity.ok(result);
    }

    /**
     * Finds Account's limits.
     * @param accountId Account id whose limits will be returned.
     * @return {@link Map} of keys: {@link ExpenseCategory}, values: {@link AccountLimitDto}
     * @throws TransactionProcessingException If there is no account for provided {@code accountId}.
     */
    @GetMapping("/{accountId}/limits")
    public ResponseEntity<Map<ExpenseCategory, AccountLimitDto>> getAccountLimits(@PathVariable int accountId)
            throws TransactionProcessingException {
        Map<ExpenseCategory, AccountLimitDto> result = accountService.getAccountLimits(accountId);
        return ResponseEntity.ok(result);
    }
}
