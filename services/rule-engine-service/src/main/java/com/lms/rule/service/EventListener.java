package com.lms.rule.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.rule.model.Rule;
import com.lms.rule.repository.RuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventListener {

    private final RuleRepository ruleRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "event.occurrence.queue")
    public void handleEvent(Map<String, Object> event) {
        log.info("Received event: {}", event);
        String eventType = (String) event.get("eventType");

        // Simple evaluation: Find all active EVENT rules that match the eventType
        List<Rule> rules = ruleRepository.findAll().stream()
                .filter(r -> "EVENT".equals(r.getRuleType()))
                .filter(Rule::getIsActive)
                .filter(r -> matchesCondition(r, eventType))
                .toList();

        for (Rule rule : rules) {
            processRuleAction(rule, event);
        }
    }

    private boolean matchesCondition(Rule rule, String eventType) {
        try {
            if (rule.getConditions() == null || rule.getConditions().isMissingNode()) {
                return false;
            }
            String conditionEventType = rule.getConditions().path("eventType").asText();
            return eventType.equals(conditionEventType);
        } catch (Exception e) {
            log.error("Failed to parse rule conditions for rule {}", rule.getId(), e);
            return false;
        }
    }

    private void processRuleAction(Rule rule, Map<String, Object> event) {
        try {
            if (rule.getActions() == null || rule.getActions().isMissingNode()) {
                log.warn("No actions defined for rule {}", rule.getId());
                return;
            }

            com.fasterxml.jackson.databind.JsonNode actionsNode = rule.getActions();
            List<Map<String, Object>> actionList;

            if (actionsNode.isArray()) {
                actionList = objectMapper.convertValue(actionsNode,
                        new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {
                        });
            } else if (actionsNode.isObject()) {
                Map<String, Object> singleAction = objectMapper.convertValue(actionsNode, Map.class);
                actionList = List.of(singleAction);
            } else {
                log.warn("Unknown actions format for rule {}", rule.getId());
                return;
            }

            for (Map<String, Object> action : actionList) {
                String type = (String) action.get("type");
                if ("AWARD_POINTS".equals(type)) {
                    executeAwardPoints(action, event, rule);
                } else if ("TIERED_POINTS".equals(type)) {
                    executeTieredPoints(action, event, rule);
                } else {
                    // Fallback for old hardcoded rules if they haven't been migrated in DB
                    // This allows backward compatibility if someone still has {"points": 100}
                    if (action.containsKey("points")) {
                        executeLegacyAwardPoints(action, event, rule.getRuleName());
                    } else {
                        log.warn("Unknown action type: {} in rule {}", type, rule.getId());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse rule actions for rule {}", rule.getId(), e);
        }
    }

    private void executeTieredPoints(Map<String, Object> action, Map<String, Object> event, Rule rule) {
        Object amountObj = event.getOrDefault("amount", event.get("value"));
        if (amountObj == null) {
            log.warn("Neither 'amount' nor 'value' field found in event for tiered rule {}", rule.getRuleName());
            return;
        }

        double amount = ((Number) amountObj).doubleValue();
        List<Map<String, Object>> ranges = (List<Map<String, Object>>) action.get("ranges");
        if (ranges == null || ranges.isEmpty()) {
            return;
        }

        for (Map<String, Object> range : ranges) {
            double min = ((Number) range.getOrDefault("min", 0.0)).doubleValue();
            Number maxNum = (Number) range.get("max");
            Double max = (maxNum != null) ? maxNum.doubleValue() : null;

            boolean inRange = amount >= min;
            if (max != null && amount > max) {
                inRange = false;
            }

            if (inRange) {
                int points = ((Number) range.getOrDefault("points", 0)).intValue();
                double multiplier = ((Number) range.getOrDefault("multiplier", 0.0)).doubleValue();
                int calculatedPoints = points;
                if (multiplier > 0) {
                    calculatedPoints += (int) (amount * multiplier);
                }

                if (calculatedPoints > 0) {
                    executeAwardPoints(Map.of(
                            "points", calculatedPoints,
                            "reason", range.getOrDefault("reason", rule.getRuleName()),
                            "memberIdField", action.getOrDefault("memberIdField", "memberId"),
                            "transactionIdField", action.getOrDefault("transactionIdField", "transactionId")),
                            event, rule);
                    log.info("Applied tiered event range: [{} - {}] -> {} points", min, (max != null ? max : "∞"),
                            calculatedPoints);
                }
                break;
            }
        }
    }

    private void executeAwardPoints(Map<String, Object> action, Map<String, Object> event, Rule rule) {
        Integer points = (Integer) action.get("points");
        String reason = (String) action.get("reason");

        // Override with entity fields if present (new advanced rules)
        if ("POINTS".equals(rule.getRewardType())) {
            // If the rule has explicit fields, we can use them
            // But usually points will still be in the action JSON for flexibility
            // Unless we add a 'points' field to the Rule entity too.
            // Let's assume for now they are in the JSON for the award amount.
        }

        String memberIdField = (String) action.getOrDefault("memberIdField", "memberId");
        String transactionIdField = (String) action.getOrDefault("transactionIdField", "transactionId");

        processAward(event, points, reason, memberIdField, transactionIdField, rule.getRuleName());
    }

    private void executeLegacyAwardPoints(Map<String, Object> action, Map<String, Object> event, String ruleName) {
        Integer points = (Integer) action.get("points");
        String reason = (String) action.get("reason");

        // Default fields for legacy
        String memberIdField = "memberId";
        String transactionIdField = "transactionId";

        // Special case for REFERRAL in legacy if it was still using this method
        if ("REFERRAL".equals(event.get("eventType"))) {
            // Legacy referral logic had two distinct awards in one rule
            // We should ideally split them into two rules or two actions
            // But for now, we'll let the new generic system handle it if migrated properly
            log.warn("Legacy REFERRAL event detected. Please migrate to generic AWARD_POINTS actions.");
        }

        processAward(event, points, reason, memberIdField, transactionIdField, ruleName);
    }

    private void processAward(Map<String, Object> event, Integer points, String reason, String memberIdField,
            String transactionIdField, String ruleName) {
        Object memberId = event.get(memberIdField);
        Object transactionId = event.get(transactionIdField);

        if (memberId == null) {
            log.warn("Member ID field '{}' not found in event for rule {}", memberIdField, ruleName);
            return;
        }

        rabbitTemplate.convertAndSend("points.earned.exchange", "", Map.of(
                "memberId", memberId,
                "transactionId", transactionId != null ? transactionId : 0L,
                "pointsEarned", points != null ? points : 0,
                "reason", reason != null ? reason : ruleName));

        log.info("Points award processed for rule {}: {} points to member {}", ruleName, points, memberId);
    }
}
