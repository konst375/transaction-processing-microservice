package com.chirko.transactionprocessing.dto.mapper;

import com.chirko.transactionprocessing.dto.TransactionDto;
import com.chirko.transactionprocessing.model.postgres.TransactionalOperation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TransactionalOperationMapper {

    TransactionalOperationMapper INSTANCE = Mappers.getMapper(TransactionalOperationMapper.class);

    @Mapping(source = "transaction.id", target = "id")
    @Mapping(expression = "java(transaction.getAccountFrom().getId())", target = "accountFrom")
    @Mapping(expression = "java(transaction.getAccountTo().getId())", target = "accountTo")
    @Mapping(source = "transaction.sum", target = "sum")
    @Mapping(expression = "java(transaction.getLimit().getSum())", target = "limitSum")
    @Mapping(source = "transaction.expenseCategory", target = "expenseCategory")
    @Mapping(source = "transaction.datetime", target = "datetime")
    @Mapping(expression = "java(transaction.getLimit().getDatetime())", target = "limitDatetime")
    @Mapping(source = "transaction.currencyShortname", target = "currencyShortname")
    @Mapping(expression = "java(transaction.getLimit().getCurrencyShortname().toString())", target = "limitCurrencyShortname")
    TransactionDto completedTransactionDtoFrom(TransactionalOperation transaction);
}
