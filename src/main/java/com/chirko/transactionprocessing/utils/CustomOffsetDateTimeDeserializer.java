package com.chirko.transactionprocessing.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CustomOffsetDateTimeDeserializer extends JsonDeserializer<DatetimePair> {

    @Override
    public final DatetimePair deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        final String value = jsonParser.getText();
        try {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssX");
            final OffsetDateTime datetime = OffsetDateTime.parse(value, formatter);
            return new DatetimePair(datetime, value);
        } catch (DateTimeParseException e) {
            return new DatetimePair(null, value);
        }
    }
}
