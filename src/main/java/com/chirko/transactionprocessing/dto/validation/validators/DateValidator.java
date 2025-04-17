package com.chirko.transactionprocessing.dto.validation.validators;

import com.chirko.transactionprocessing.dto.validation.constraints.ValidDate;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateValidator implements ConstraintValidator<ValidDate, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssX");
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(value, formatter);
            Timestamp datetime = Timestamp.from(zonedDateTime.toInstant());
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }
}
