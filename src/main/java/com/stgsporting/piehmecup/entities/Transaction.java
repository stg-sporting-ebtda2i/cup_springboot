package com.stgsporting.piehmecup.entities;

import com.stgsporting.piehmecup.config.DatabaseEnum;
import com.stgsporting.piehmecup.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Setter
@Getter
@Entity(name = DatabaseEnum.transactionsTable)
@Table(indexes = @Index(name = "idx_transaction_description", columnList = DatabaseEnum.transactionDescription))
public class Transaction extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = DatabaseEnum.transactionUserId, nullable = false)
    private User user;

    @Column(name = DatabaseEnum.amount, nullable = false)
    private Integer amount;

    @Column(name = DatabaseEnum.transactionType, nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(name = DatabaseEnum.transactionDescription)
    private String description;

    @Column(name = DatabaseEnum.transactionDate, nullable = false)
    private Timestamp createdAt;
}
