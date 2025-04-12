package com.chirko.transactionprocessing.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Account extends AbstractEntity {

    @Builder.Default
    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal balance = BigDecimal.valueOf(0);

    @OneToMany(mappedBy = "accountFrom")
    private List<TransactionalOperation> expenseTransactions;

    @OneToMany(mappedBy = "accountTo")
    private List<TransactionalOperation> incomeTransactions;

    @OneToMany(mappedBy = "account")
    private List<AccountLimit> accountLimits;

    @UpdateTimestamp
    @Column(nullable = false)
    private Timestamp updatedDatetime;
}
