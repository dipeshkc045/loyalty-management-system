package com.lms.transaction.event;

import com.lms.transaction.model.Transaction;
import com.lms.transaction.model.TransactionStatus;
import com.lms.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionEventListener {
    private final TransactionRepository transactionRepository;

    @RabbitListener(queues = "points.earned.queue")
    public void handlePointsEarned(PointsEarnedEvent event) {
        log.info("Received points earned event for transaction {}", event.getTransactionId());

        transactionRepository.findById(event.getTransactionId()).ifPresentOrElse(transaction -> {
            transaction.setPointsEarned(event.getPointsEarned());
            transaction.setStatus(TransactionStatus.COMPLETED);
            transactionRepository.save(transaction);
            log.info("Updated transaction {} status to COMPLETED with {} points", transaction.getId(),
                    event.getPointsEarned());
        }, () -> {
            log.error("Transaction {} not found for points update", event.getTransactionId());
        });
    }
}
