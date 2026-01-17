package com.lms.notification.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class NotificationConsumer {

    @RabbitListener(queues = "notification.points.queue")
    public void handlePointsNotification(Map<String, Object> event) {
        log.info("NOTIFICATION: Member {} earned {} points for: {}",
                event.get("memberId"), event.get("pointsEarned"), event.get("reason"));
    }
}
