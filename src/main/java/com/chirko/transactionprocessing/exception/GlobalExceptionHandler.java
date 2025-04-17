package com.chirko.transactionprocessing.exception;

import com.chirko.transactionprocessing.dto.TransactionProcessingExceptionDto;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, List<String>>> handleValidationErrors(HttpServletRequest request, MethodArgumentNotValidException e) {
        logger.warn(e.getAllErrors().toString());
        final List<String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        final Map<String, List<String>> errorsMap = getErrorsMap(errors);
        return new ResponseEntity<>(errorsMap, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TransactionProcessingException.class)
    public ResponseEntity<TransactionProcessingExceptionDto> handleTransactionProcessingException(HttpServletRequest request, TransactionProcessingException e) {
        logger.warn(e.toString());
        TransactionProcessingExceptionDto exceptionDto = new TransactionProcessingExceptionDto(
                Timestamp.valueOf(LocalDateTime.now()),
                e.getErrorCode().getCode(),
                e.getMessage(),
                e.getErrorCode().getHttpStatus().value(),
                e.getErrorCode().getHttpStatus(),
                e.getErrorCode().getHttpStatus().getReasonPhrase(),
                request.getRequestURI());
        return new ResponseEntity<>(exceptionDto, e.getErrorCode().getHttpStatus());
    }

    private Map<String, List<String>> getErrorsMap(List<String> errors) {
        Map<String, List<String>> errorResponse = new HashMap<>();
        errorResponse.put("errors", errors);
        return errorResponse;
    }
}
