package com.chirko.transactionprocessing.dto.mapper;

import com.chirko.transactionprocessing.dto.AccountDto;
import com.chirko.transactionprocessing.model.postgres.Account;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {AccountLimitMapper.class})
public interface AccountMapper {

    AccountMapper INSTANCE = Mappers.getMapper(AccountMapper.class);

    AccountDto accountDtoFrom(Account account);
}
