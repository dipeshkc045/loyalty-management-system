package com.lms.transaction.event;

import com.lms.transaction.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionEventProducer {
    private final RabbitTemplate rabbitTemplate;

    public void sendTransactionCreated(TransactionEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.TRANSACTION_EXCHANGE,
                RabbitMQConfig.TRANSACTION_CREATED_ROUTING_KEY, event);
    }
}
