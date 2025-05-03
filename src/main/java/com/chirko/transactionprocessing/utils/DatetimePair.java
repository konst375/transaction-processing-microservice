package com.chirko.transactionprocessing.utils;

import java.time.OffsetDateTime;

public record DatetimePair(
        OffsetDateTime datetime,
        String providedUnparsedValue
) {
}
