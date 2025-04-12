package com.chirko.transactionprocessing.dto.mapper;

import com.chirko.transactionprocessing.dto.CompletedTransactionDto;
import com.chirko.transactionprocessing.model.AccountLimit;
import com.chirko.transactionprocessing.model.TransactionalOperation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface TransactionalOperationMapper {
    @Mapping(expression = "java(transaction.getAccountFrom().getId())", target = "accountFrom")
    @Mapping(expression = "java(transaction.getAccountTo().getId())", target = "accountTo")
    @Mapping(source = "transaction.sum", target = "sum")
    @Mapping(source = "limit.sum", target = "limitSum")
    @Mapping(source = "transaction.expenseCategory", target = "expenseCategory")
    @Mapping(source = "transaction.datetime", target = "datetime")
    @Mapping(source = "limit.datetime", target = "limitDatetime")
    @Mapping(source = "transaction.currencyShortname", target = "currencyShortname")
    @Mapping(source = "limit.currencyShortname", target = "limitCurrencyShortname")
    CompletedTransactionDto from(TransactionalOperation transaction, AccountLimit limit);


}
