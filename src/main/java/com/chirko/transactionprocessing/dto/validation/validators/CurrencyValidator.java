package com.chirko.transactionprocessing.dto.validation.validators;

import com.chirko.transactionprocessing.dto.validation.constraints.AcceptableCurrency;
import com.chirko.transactionprocessing.model.emuns.CurrencyShortname;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class CurrencyValidator implements ConstraintValidator<AcceptableCurrency, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        final boolean isValid = Arrays.stream(CurrencyShortname.values())
                .map(CurrencyShortname::name)
                .anyMatch(t -> t.equalsIgnoreCase(value));
        if (!isValid) {
            final String message = String.format("Unacceptable currency: %s", value);
            context.buildConstraintViolationWithTemplate(message)
                    .addPropertyNode("currency_shortname")
                    .addConstraintViolation();
        }
        return isValid;
    }
}
