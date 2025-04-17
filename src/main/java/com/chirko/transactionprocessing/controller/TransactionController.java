package com.chirko.transactionprocessing.controller;

import com.chirko.transactionprocessing.dto.CompletedTransactionDto;
import com.chirko.transactionprocessing.dto.TransactionProcessingRequestDto;
import com.chirko.transactionprocessing.exception.TransactionProcessingException;
import com.chirko.transactionprocessing.service.TransactionalOperationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1/transaction-service/transactions")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    private final TransactionalOperationService transactionalOperationService;

    @PostMapping
    public ResponseEntity<CompletedTransactionDto> performTransaction(@RequestBody @Valid TransactionProcessingRequestDto requestDto)
            throws TransactionProcessingException {
        logger.info("Processing transaction POST request, requestDto: {}", requestDto);
        final CompletedTransactionDto response = transactionalOperationService.performTransaction(requestDto);

        logger.debug("Building location URI for resource {}", response);
        final URI location = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }
}
