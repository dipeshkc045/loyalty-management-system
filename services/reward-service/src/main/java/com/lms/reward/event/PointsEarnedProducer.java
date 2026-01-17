package com.lms.reward.event;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointsEarnedProducer {
    private final RabbitTemplate rabbitTemplate;

    public void sendPointsEarned(PointsEarnedEvent event) {
        rabbitTemplate.convertAndSend("points.earned.exchange", "", event);
    }
}
