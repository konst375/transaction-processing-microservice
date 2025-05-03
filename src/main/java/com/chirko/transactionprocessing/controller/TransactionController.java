package com.chirko.transactionprocessing.controller;

import com.chirko.transactionprocessing.dto.TransactionDto;
import com.chirko.transactionprocessing.dto.TransactionProcessingRequestDto;
import com.chirko.transactionprocessing.exception.TransactionProcessingException;
import com.chirko.transactionprocessing.service.TransactionalOperationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/transaction-service/transactions")
public class TransactionController {

    private final TransactionalOperationService transactionalOperationService;

    /** Performs transaction.
     * @param requestDto Submitted data.
     * @return {@link TransactionDto} for performed transaction.
     * @throws TransactionProcessingException If Account does not exist for ID provided in {@code requestDto}
     * of if exchange rate data did not find.
     */
    @PostMapping
    public ResponseEntity<TransactionDto> performTransaction(@RequestBody @Valid TransactionProcessingRequestDto requestDto)
            throws TransactionProcessingException {
        final TransactionDto response = transactionalOperationService.performTransaction(requestDto);
        final URI location = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    /** Finds performed transaction by ID.
     * @param transactionId Transaction ID.
     * @return {@link TransactionDto} for found transaction.
     * @throws TransactionProcessingException If no transaction found for provided {@code transactionId}
     */
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionDto> getById(@PathVariable int transactionId)
            throws TransactionProcessingException {
        final TransactionDto response = transactionalOperationService.getByTransactionId(transactionId);
        return ResponseEntity.ok(response);
    }

    /** Finds transactions that exceeded limit by Account ID.
     * @param accountId Account ID.
     * @return {@link List} of {@link TransactionDto} that exceeded monthly limit.
     * @throws TransactionProcessingException If there is no account for provided {@code accountId}.
     */
    @GetMapping("/limit-exceeded")
    public ResponseEntity<List<TransactionDto>> getExceededLimitForAccount(@RequestParam int accountId)
            throws TransactionProcessingException {
        List<TransactionDto> result = transactionalOperationService.getExceededLimitForAccount(accountId);
        return ResponseEntity.ok(result);
    }
}
