package com.example.relicv2.repository;

import com.example.relicv2.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserId(Long userId);
    List<Transaction> findByOrderId(Long orderId);
    @Query("SELECT AVG(t.amount) FROM Transaction t WHERE t.transactionDate BETWEEN :startDate AND :endDate")
    Double calculateAverageTransactionAmount(Date startDate, Date endDate);
}
