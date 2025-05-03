package com.chirko.transactionprocessing.exception;

import com.chirko.transactionprocessing.dto.TransactionProcessingExceptionDto;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

import static com.chirko.transactionprocessing.exception.ErrorCode.INVALID_DATA_SUBMITTED;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<TransactionProcessingExceptionDto> handleValidationErrors(
            HttpServletRequest request, MethodArgumentNotValidException exception) {
        final List<String> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .filter(Objects::nonNull)
                .filter(message -> !message.isBlank())
                .toList();
        final StringBuilder messageBuilder = new StringBuilder();
        for (int i = 0; i < errors.size(); i++) {
            messageBuilder.append(errors.get(i));
            if (i != errors.size() - 1) {
                messageBuilder.append("; ");
            }
        }

        final TransactionProcessingExceptionDto exceptionDto = TransactionProcessingExceptionDto.builder()
                .datetime(OffsetDateTime.now())
                .errorCode(INVALID_DATA_SUBMITTED.getCode())
                .message(INVALID_DATA_SUBMITTED.getFormat() + messageBuilder)
                .statusCode(INVALID_DATA_SUBMITTED.getHttpStatus().value())
                .httpStatus(INVALID_DATA_SUBMITTED.getHttpStatus())
                .exception(exception.getClass().getName())
                .path(request.getRequestURI())
                .build();

        final ResponseEntity<TransactionProcessingExceptionDto> response =
                new ResponseEntity<>(exceptionDto, exceptionDto.httpStatus());

        logger.error("""
                        Submitted data validation exception caught:
                        errors: {},
                        response: {}""",
                exception.getAllErrors(), response);
        return response;
    }

    @ExceptionHandler(TransactionProcessingException.class)
    public ResponseEntity<TransactionProcessingExceptionDto> handleTransactionProcessingException(
            HttpServletRequest request, TransactionProcessingException exception) {
        final TransactionProcessingExceptionDto exceptionDto = TransactionProcessingExceptionDto.builder()
                .datetime(OffsetDateTime.now())
                .errorCode(exception.getErrorCode().getCode())
                .message(exception.getMessage())
                .statusCode(exception.getErrorCode().getHttpStatus().value())
                .httpStatus(exception.getErrorCode().getHttpStatus())
                .exception(exception.getClass().getName())
                .path(request.getRequestURI())
                .build();

        final ResponseEntity<TransactionProcessingExceptionDto> response =
                new ResponseEntity<>(exceptionDto, exception.getErrorCode().getHttpStatus());

        logger.error("""
                        Exception caught:
                        exception: {},
                        exceptionDto: {},
                        response: {}""",
                exception, exceptionDto, response);
        return response;
    }
}
