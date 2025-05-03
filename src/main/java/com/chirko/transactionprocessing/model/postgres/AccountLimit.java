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
public class AccountLimit extends AbstractEntity {

    private static final BigDecimal DEFAULT_LIMIT_SUM = BigDecimal.valueOf(1000);

    @ManyToOne
    @JoinColumn(name = "account")
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseCategory expenseCategory;

    @Builder.Default
    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal sum = DEFAULT_LIMIT_SUM;
}
