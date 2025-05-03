package com.chirko.transactionprocessing.dto.validation.validators;

import com.chirko.transactionprocessing.dto.validation.constraints.ValidDatetimeFormat;
import com.chirko.transactionprocessing.utils.DatetimePair;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DatetimeValidator implements ConstraintValidator<ValidDatetimeFormat, DatetimePair> {

    @Override
    public boolean isValid(DatetimePair datetimePair, ConstraintValidatorContext context) {
        final boolean isValid = datetimePair.datetime() != null;
        if (!isValid) {
            final String errorMessage = String.format(
                    "Invalid datetime format, mast be in format like: " +
                    "2022-01-30 00:00:00+06, provided value: %s", datetimePair.providedUnparsedValue());
            context.buildConstraintViolationWithTemplate(errorMessage)
                    .addPropertyNode("datetime")
                    .addConstraintViolation();
        }
        return isValid;
    }
}
