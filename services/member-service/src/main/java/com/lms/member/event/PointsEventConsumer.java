package com.lms.member.event;

import com.lms.member.model.Member;
import com.lms.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.lms.member.service.MemberService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PointsEventConsumer {
    private final MemberService memberService;

    @RabbitListener(queues = "member.points.queue")
    public void handlePointsEarned(Map<String, Object> event) {
        log.info("Received points earned event: {}", event);

        Long memberId = ((Number) event.get("memberId")).longValue();
        Object txIdObj = event.get("transactionId");
        Long transactionId = (txIdObj != null) ? ((Number) txIdObj).longValue() : 0L;
        int points = ((Number) event.get("pointsEarned")).intValue();

        memberService.awardPoints(memberId, points, transactionId);

        // Tier upgrade logic remains in service or here (service is better for
        // consistency)
        log.info("Points awarded for member {}", memberId);
    }
}
