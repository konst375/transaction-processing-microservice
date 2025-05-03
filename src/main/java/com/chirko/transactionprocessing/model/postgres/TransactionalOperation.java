package com.chirko.transactionprocessing.model.postgres;

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
@EqualsAndHashCode(callSuper = true)
public class TransactionalOperation extends AbstractEntity {

    @ManyToOne
    @JoinColumn(name = "account_to")
    private Account accountTo;

    @ManyToOne
    @JoinColumn(name = "account_from")
    private Account accountFrom;

    @ManyToOne
    @JoinColumn(name = "corresponding_limit")
    private AccountLimit limit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseCategory expenseCategory;

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal sum;

    @Builder.Default
    private boolean limitExceeded = false;

    private BigDecimal remainingMonthlyLimit;
}
