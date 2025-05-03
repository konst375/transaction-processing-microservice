package com.chirko.transactionprocessing.model.postgres;

import com.chirko.transactionprocessing.model.emuns.ExpenseCategory;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Account extends AbstractEntity {

    @Builder.Default
    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal balance = BigDecimal.valueOf(0);

    @OneToMany(mappedBy = "accountFrom")
    private List<TransactionalOperation> expenseTransactions;

    @OneToMany(mappedBy = "accountTo")
    private List<TransactionalOperation> incomeTransactions;

    @OneToMany(mappedBy = "account", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @MapKeyColumn(name = "expenseCategory")
    @MapKeyEnumerated(EnumType.STRING)
    private Map<ExpenseCategory, AccountLimit> accountLimits;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime updatedDatetime;

    public AccountLimit getRemainingLimitForCategory(ExpenseCategory expenseCategory) {
        return accountLimits.get(expenseCategory);
    }

}
