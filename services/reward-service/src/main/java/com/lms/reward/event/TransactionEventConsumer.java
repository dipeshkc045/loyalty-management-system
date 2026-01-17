package com.lms.reward.event;

import com.lms.reward.client.RuleClient;
import com.lms.reward.service.TieredPointCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionEventConsumer {
        private final TieredPointCalculationService tieredPointCalculationService;
        private final PointsEarnedProducer pointsEarnedProducer;
        private final RuleClient ruleClient;
        private final com.lms.reward.client.TransactionClient transactionClient;

        @RabbitListener(queues = "transaction.created.queue")
        public void handleTransactionCreated(Map<String, Object> event) {
                log.info("Received transaction event: {}", event);

                Long memberId = ((Number) event.get("memberId")).longValue();
                Long transactionId = ((Number) event.get("transactionId")).longValue();
                java.math.BigDecimal amount = java.math.BigDecimal
                                .valueOf(((Number) event.get("amount")).doubleValue());
                String paymentMethod = (String) event.get("paymentMethod");
                String category = (String) event.get("productCategory");

                // Orchestration: Fetch member and activity (mocked/simplified for now)
                com.lms.reward.model.TransactionFact fact = com.lms.reward.model.TransactionFact.builder()
                                .memberId(memberId)
                                .amount(amount)
                                .paymentMethod(paymentMethod)
                                .productCategory(category)
                                .memberTier("BRONZE") // In production, fetch from MemberClient
                                .role("CUSTOMER") // In production, fetch from MemberClient
                                .build();

                // Fetch real activity summaries
                com.lms.reward.model.TransactionSummary monthly = transactionClient.getSummary(memberId, "MONTHLY");
                com.lms.reward.model.TransactionSummary quarterly = transactionClient.getSummary(memberId, "QUARTERLY");

                com.lms.reward.model.MemberActivityFact activity = com.lms.reward.model.MemberActivityFact.builder()
                                .memberId(memberId)
                                .monthlyTransactionCount(monthly.getTransactionCount())
                                .monthlyTotalSpent(monthly.getTotalAmount())
                                .quarterlyTransactionCount(quarterly.getTransactionCount())
                                .quarterlyTotalSpent(quarterly.getTotalAmount())
                                .transactionCount(monthly.getTransactionCount())
                                .totalSpent(monthly.getTotalAmount())
                                .build();

                com.lms.reward.model.RuleEvaluationRequest request = com.lms.reward.model.RuleEvaluationRequest
                                .builder()
                                .transaction(fact)
                                .activity(activity)
                                .build();

                // Evaluate rules via Drools service (for bonus points only)
                com.lms.reward.model.TransactionFact evaluatedFact = ruleClient.evaluateRules(request);

                // Calculate base points using tiered system
                int basePoints = tieredPointCalculationService.calculatePoints(amount, fact.getMemberTier());

                // Add any bonus points from rules
                int finalPoints = basePoints + evaluatedFact.getBonusPoints();

                log.info("Calculated rewards for transaction {}: BasePoints={}, BonusPoints={}, TotalPoints={}, Tier={}, RewardType={}, Discount={}%",
                                transactionId, basePoints, evaluatedFact.getBonusPoints(), finalPoints,
                                fact.getMemberTier(), evaluatedFact.getRewardType(),
                                evaluatedFact.getDiscountPercentage());

                pointsEarnedProducer.sendPointsEarned(com.lms.reward.event.PointsEarnedEvent.builder()
                                .memberId(memberId)
                                .transactionId(transactionId)
                                .pointsEarned(finalPoints)
                                .reason("Tiered loyalty points (Tier: " + fact.getMemberTier() + ")")
                                .build());
        }
}
