package com.chirko.transactionprocessing.model.postgres;

import com.chirko.transactionprocessing.model.emuns.CurrencyShortname;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;

@Getter
@Setter
@SuperBuilder
@MappedSuperclass
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class AbstractEntity {
    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CurrencyShortname currencyShortname;

    @Builder.Default
    @Column(nullable = false)
    private OffsetDateTime datetime = OffsetDateTime.now();
}
