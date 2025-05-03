package com.chirko.transactionprocessing.dto.validation.validators;

import com.chirko.transactionprocessing.dto.validation.constraints.AcceptableCategory;
import com.chirko.transactionprocessing.model.emuns.ExpenseCategory;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class CategoryValidator implements ConstraintValidator<AcceptableCategory, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        final boolean isValid = Arrays.stream(ExpenseCategory.values())
                .map(ExpenseCategory::name)
                .anyMatch(t -> t.equalsIgnoreCase(value));
        if (!isValid) {
            final String message = String.format("Unacceptable transaction category: %s", value);
            context.buildConstraintViolationWithTemplate(message)
                    .addPropertyNode("category")
                    .addConstraintViolation();
        }
        return isValid;
    }
}
