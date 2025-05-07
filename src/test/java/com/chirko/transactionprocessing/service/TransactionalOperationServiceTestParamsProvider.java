package com.chirko.transactionprocessing.service;

import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

public class TransactionalOperationServiceTestParamsProvider {

    public static Stream<Arguments> checkIfLimitExceededParamsProvider() {
        return Stream.of(
                arguments(1000, 900, false),
                arguments(900, 1000, true),
                arguments(0, 1000, true),
                arguments(-1000, 900, false)
        );
    }
}
