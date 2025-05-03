package com.chirko.transactionprocessing.dto.mapper;

import com.chirko.transactionprocessing.dto.AccountLimitDto;
import com.chirko.transactionprocessing.model.postgres.AccountLimit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AccountLimitMapper {

    AccountLimitMapper INSTANCE = Mappers.getMapper(AccountLimitMapper.class);

    @Mapping(expression = "java(limit.getAccount().getId())", target = "account")
    AccountLimitDto accountLimitDtoFrom(AccountLimit limit);
}
