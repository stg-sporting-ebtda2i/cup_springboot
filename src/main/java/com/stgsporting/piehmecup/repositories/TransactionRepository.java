package com.stgsporting.piehmecup.repositories;

import com.stgsporting.piehmecup.entities.Transaction;
import com.stgsporting.piehmecup.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("SELECT DISTINCT t.description FROM TRANSACTIONS t WHERE t.type = :type AND t.description IN :descriptions")
    Set<String> findExistingDescriptions(@Param("type") TransactionType type, @Param("descriptions") List<String> descriptions);
}
