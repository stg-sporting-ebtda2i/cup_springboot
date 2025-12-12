package com.stgsporting.piehmecup.entities;

import com.stgsporting.piehmecup.config.DatabaseEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = DatabaseEnum.pricesTable)
@Table(
        name = DatabaseEnum.pricesTable,
        indexes = {
                @Index(name = "idx_level_name", columnList = DatabaseEnum.levelId + ", " + DatabaseEnum.liturgyName, unique = true),
        }
)
public class Price extends BaseEntity {

    @Column(name = DatabaseEnum.liturgyName, nullable = false)
    private String name;

    @Column(name = DatabaseEnum.coins, nullable = false)
    private Integer coins;

    @ManyToOne
    @JoinColumn(name = DatabaseEnum.levelId, nullable = false)
    private Level level;

    @Column(name = DatabaseEnum.visible, nullable = false)
    private boolean visible;
}
