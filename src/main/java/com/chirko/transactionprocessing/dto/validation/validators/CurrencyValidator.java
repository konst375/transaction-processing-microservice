package com.chirko.transactionprocessing.dto.validation.validators;

import com.chirko.transactionprocessing.dto.validation.constraints.AcceptableCurrency;
import com.chirko.transactionprocessing.model.emuns.CurrencyShortname;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class CurrencyValidator implements ConstraintValidator<AcceptableCurrency, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return Arrays.stream(CurrencyShortname.values())
                .map(CurrencyShortname::name)
                .anyMatch(t -> t.equalsIgnoreCase(value));
    }
}
