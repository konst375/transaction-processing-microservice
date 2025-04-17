package com.chirko.transactionprocessing.model.postgres;

import com.chirko.transactionprocessing.model.emuns.CurrencyShortname;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Getter
@Setter
@SuperBuilder
@MappedSuperclass
@NoArgsConstructor
public abstract class AbstractEntity {
    @Id
    @Column(nullable = false)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CurrencyShortname currencyShortname;

    @CreationTimestamp
    @Column(nullable = false)
    private Timestamp datetime;
}
