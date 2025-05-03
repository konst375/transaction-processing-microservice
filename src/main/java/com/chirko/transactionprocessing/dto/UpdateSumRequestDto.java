package com.chirko.transactionprocessing.dto;

import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record UpdateSumRequestDto(
        @PositiveOrZero
        BigDecimal sum
) {
}
