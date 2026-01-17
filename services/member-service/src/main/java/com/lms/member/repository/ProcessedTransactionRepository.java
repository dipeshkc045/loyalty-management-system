package com.lms.member.repository;

import com.lms.member.model.ProcessedTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProcessedTransactionRepository extends JpaRepository<ProcessedTransaction, Long> {
    Optional<ProcessedTransaction> findByTransactionId(Long transactionId);
}
