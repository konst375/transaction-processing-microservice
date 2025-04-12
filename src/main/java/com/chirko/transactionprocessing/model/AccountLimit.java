package com.chirko.transactionprocessing.model;

import com.chirko.transactionprocessing.model.emuns.ExpenseCategory;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AccountLimit extends AbstractEntity {

    @ManyToOne
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseCategory expenseCategory;

    @Builder.Default
    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal sum = BigDecimal.valueOf(1000);

}
