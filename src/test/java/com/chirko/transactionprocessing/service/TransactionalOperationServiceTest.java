package com.chirko.transactionprocessing.service;

import com.chirko.transactionprocessing.dto.mapper.TransactionalOperationMapper;
import com.chirko.transactionprocessing.model.emuns.ExpenseCategory;
import com.chirko.transactionprocessing.model.postgres.Account;
import com.chirko.transactionprocessing.model.postgres.AccountLimit;
import com.chirko.transactionprocessing.repository.postgres.TransactionalOperationRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {TransactionalOperationService.class})
class TransactionalOperationServiceTest {

    @Autowired
    @InjectMocks
    private TransactionalOperationService transactionalOperationService;

    @Mock
    private TransactionalOperationRepository repository;

    @Mock
    private ExchangeRateService exchangeRateService;

    @Mock
    private AccountService accountService;

    private final TransactionalOperationMapper mapper = TransactionalOperationMapper.INSTANCE;

    @ParameterizedTest
    @MethodSource("com.chirko.transactionprocessing.service.TransactionalOperationServiceTestParamsProvider#checkIfLimitExceededParamsProvider")
    void checkIfLimitExceeded(long balance, long sum, boolean limitExceeded) {
        //given
        final ExpenseCategory category = ExpenseCategory.valueOf("PRODUCT");
        final OffsetDateTime datetime = OffsetDateTime.now();
        final BigDecimal targetBalance = BigDecimal.valueOf(balance);
        final Account account = Account.builder()
                .id(1)
                .accountLimits(Map.of(category, AccountLimit.builder().sum(targetBalance).build()))
                .build();

        final BigDecimal targetSum = BigDecimal.valueOf(sum);
        final BigDecimal remains = targetBalance.subtract(targetSum);

        //when
        TransactionalOperationService.LimitExceededCheckResult result =
                transactionalOperationService.checkIfLimitExceeded(category, datetime, account, targetSum);

        //then
        Assertions.assertEquals(result.remains(), remains);
        Assertions.assertEquals(result.limitExceeded(), limitExceeded);
    }

}