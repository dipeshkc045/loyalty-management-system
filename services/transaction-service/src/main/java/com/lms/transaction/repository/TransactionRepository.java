package com.lms.transaction.repository;

import com.lms.transaction.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
        List<Transaction> findByMemberId(Long memberId);

        // New: Find transactions by member ID sorted by date
        List<Transaction> findAllByMemberIdOrderByTransactionDateDesc(Long memberId);

        @org.springframework.data.jpa.repository.Query("SELECT new com.lms.transaction.model.TransactionSummary(t.memberId, COUNT(t), SUM(t.amount)) "
                        +
                        "FROM Transaction t WHERE t.memberId = :memberId AND t.transactionDate >= :since " +
                        "GROUP BY t.memberId")
        java.util.Optional<com.lms.transaction.model.TransactionSummary> getSummary(Long memberId,
                        java.time.LocalDateTime since);

        // New: Get summary for a specific month
        @org.springframework.data.jpa.repository.Query("SELECT new com.lms.transaction.model.TransactionSummary(t.memberId, COUNT(t), SUM(t.amount)) "
                        +
                        "FROM Transaction t WHERE t.memberId = :memberId AND t.transactionDate BETWEEN :startDate AND :endDate "
                        +
                        "GROUP BY t.memberId")
        java.util.Optional<com.lms.transaction.model.TransactionSummary> getMonthlySummary(Long memberId,
                        java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
}
