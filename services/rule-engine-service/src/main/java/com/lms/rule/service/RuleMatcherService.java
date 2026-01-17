package com.lms.rule.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.lms.rule.model.Rule;
import com.lms.rule.model.TransactionFact;
import com.lms.rule.model.MemberActivityFact;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleMatcherService {
    private final RuleLoaderService ruleLoaderService;
    private final com.lms.rule.repository.RuleAuditRepository ruleAuditRepository;
    private final io.micrometer.core.instrument.MeterRegistry meterRegistry;
    private final com.lms.rule.repository.RuleRepository ruleRepository;

    public TransactionFact evaluateRules(TransactionFact fact, MemberActivityFact activity) {
        meterRegistry.counter("loyalty.rules.evaluated.total").increment();
        log.info("Evaluating rules for fact: {} and activity: {}", fact, activity);

        // 1. Evaluate Field-Based Rules (Simple Rules)
        List<Rule> simpleRules = ruleRepository.findAll().stream()
                .filter(Rule::getIsActive)
                .filter(r -> "SIMPLE".equals(r.getRuleType()) || "TRANSACTION".equals(r.getRuleType()))
                .toList();

        for (Rule rule : simpleRules) {
            applySimpleRule(rule, fact, activity);
        }

        // 2. Evaluate Drools Rules
        KieSession kieSession = ruleLoaderService.getKieContainer().newKieSession();

        // Add audit listener
        kieSession.addEventListener(new org.kie.api.event.rule.DefaultAgendaEventListener() {
            @Override
            public void afterMatchFired(org.kie.api.event.rule.AfterMatchFiredEvent event) {
                String ruleName = event.getMatch().getRule().getName();
                log.info("Rule fired: {}", ruleName);
                ruleAuditRepository.save(com.lms.rule.model.RuleAudit.builder()
                        .memberId(fact.getMemberId())
                        .ruleName(ruleName)
                        .resultType("GENERAL")
                        .resultValue("Fired")
                        .build());
            }
        });

        try {
            kieSession.insert(fact);
            if (activity != null)
                kieSession.insert(activity);
            kieSession.fireAllRules();
        } finally {
            kieSession.dispose();
        }
        log.info("Rules evaluation completed. Final multipliers: {}, Bonus points: {}",
                fact.getPointMultiplier(), fact.getBonusPoints());
        return fact;
    }

    private void applySimpleRule(Rule rule, TransactionFact fact, MemberActivityFact activity) {
        // Skip if target tier doesn't match
        if (rule.getTargetTier() != null && !rule.getTargetTier().isEmpty() &&
                !rule.getTargetTier().equalsIgnoreCase(fact.getMemberTier())) {
            log.debug("Skipping rule {} as member tier {} does not match target tier {}",
                    rule.getRuleName(), fact.getMemberTier(), rule.getTargetTier());
            return;
        }

        // Expiry check
        if (rule.getValidUntil() != null && java.time.LocalDateTime.now().isAfter(rule.getValidUntil())) {
            return;
        }

        // Product filter (Multi-product support)
        if (rule.getTargetProductCodes() != null && !rule.getTargetProductCodes().isEmpty()) {
            if (!rule.getTargetProductCodes().contains(fact.getProductCategory())) {
                return;
            }
        } else if (rule.getTargetProductCode() != null && !rule.getTargetProductCode().isEmpty()) {
            // Fallback to legacy field
            if (!rule.getTargetProductCode().equals(fact.getProductCategory())) {
                return;
            }
        }

        // Amount range filter
        if (rule.getMinAmount() != null && fact.getAmount().compareTo(rule.getMinAmount()) < 0) {
            return;
        }
        if (rule.getMaxAmount() != null && fact.getAmount().compareTo(rule.getMaxAmount()) > 0) {
            return;
        }

        // Volume/Amount aggregates
        if (activity != null && rule.getEvaluationType() != null) {
            long currentVolume = 0;
            java.math.BigDecimal currentTotalSpent = java.math.BigDecimal.ZERO;

            if ("MONTHLY".equals(rule.getEvaluationType())) {
                currentVolume = activity.getMonthlyTransactionCount();
                currentTotalSpent = activity.getMonthlyTotalSpent();
            } else if ("QUARTERLY".equals(rule.getEvaluationType())) {
                currentVolume = activity.getQuarterlyTransactionCount();
                currentTotalSpent = activity.getQuarterlyTotalSpent();
            } else {
                // For TRANSACTION evaluation type, we might still want to check
                // lifetime/default aggregates
                currentVolume = activity.getTransactionCount();
                currentTotalSpent = activity.getTotalSpent();
            }

            if (rule.getMinVolume() != null && currentVolume < rule.getMinVolume()) {
                return;
            }
            if (rule.getMaxVolume() != null && currentVolume > rule.getMaxVolume()) {
                return;
            }
            if (rule.getMinAmount() != null && currentTotalSpent != null
                    && currentTotalSpent.compareTo(rule.getMinAmount()) < 0) {
                // Note: minAmount can be used for both per-transaction and aggregate
                // If evaluationType is TRANSACTION, it checks per-transaction amount (already
                // done above)
                // If evaluationType is MONTHLY, it checks aggregate amount.
                // Re-check logic: if it's periodic, currentTotalSpent is the aggregate.
                if (!"TRANSACTION".equals(rule.getEvaluationType())) {
                    return;
                }
            }
        }

        // Apply reward
        log.info("Applying simple rule: {}", rule.getRuleName());
        try {
            log.info("Rule actions for {}: {}", rule.getRuleName(), rule.getActions());
            if (rule.getActions() == null || rule.getActions().isMissingNode() || rule.getActions().isEmpty())
                return;

            JsonNode actionsNode = rule.getActions();
            if (!actionsNode.isArray()) {
                log.warn("Actions for rule {} is not an array: {}", rule.getRuleName(), actionsNode);
                return;
            }

            for (JsonNode action : actionsNode) {
                String type = action.path("type").asText();

                if ("TIERED_POINTS".equals(type)) {
                    JsonNode ranges = action.path("ranges");
                    if (ranges.isArray()) {
                        for (JsonNode range : ranges) {
                            java.math.BigDecimal min = new java.math.BigDecimal(range.path("min").asDouble(0.0));
                            JsonNode maxNode = range.get("max");
                            java.math.BigDecimal max = (maxNode != null && !maxNode.isNull()
                                    && !maxNode.asText().isEmpty())
                                            ? new java.math.BigDecimal(maxNode.asDouble())
                                            : null;

                            log.debug("Checking range [{} - {}] for amount {}", min, max, fact.getAmount());
                            boolean inRange = fact.getAmount().compareTo(min) >= 0;
                            if (max != null && fact.getAmount().compareTo(max) > 0) {
                                inRange = false;
                            }

                            if (inRange) {
                                log.debug("Amount {} is in range [{} - {}]", fact.getAmount(), min, max);
                                int points = range.path("points").asInt(0);
                                double multiplier = range.path("multiplier").asDouble(0.0);
                                int calculatedPoints = points;
                                if (multiplier > 0) {
                                    calculatedPoints += (int) (fact.getAmount().doubleValue() * multiplier);
                                }
                                if (calculatedPoints > 0) {
                                    fact.setBonusPoints(fact.getBonusPoints() + calculatedPoints);
                                    log.info(
                                            "Applied tiered points range for rule {}: [{} - {}] -> {} points (Base: {}, Multiplier: {})",
                                            rule.getRuleName(), min, (max != null ? max : "∞"), calculatedPoints,
                                            points, multiplier);
                                }
                                // Stop after first matching range
                                break;
                            }
                        }
                    }
                } else if ("POINTS".equals(rule.getRewardType()) || "AWARD_POINTS".equals(type)) {
                    int points = action.path("points").asInt(0);
                    double multiplier = action.path("pointsMultiplier")
                            .asDouble(action.path("multiplier").asDouble(0.0));

                    if (multiplier > 0) {
                        points += (int) (fact.getAmount().doubleValue() * multiplier);
                        log.info("Calculated proportional points: {} (Amount: {} * Multiplier: {})",
                                points, fact.getAmount(), multiplier);
                    }

                    if (points > 0) {
                        fact.setBonusPoints(fact.getBonusPoints() + points);
                    }
                } else if ("DISCOUNT".equals(rule.getRewardType()) || "AWARD_DISCOUNT".equals(type)) {
                    fact.setRewardType("DISCOUNT");
                    double discount = action.path("discountPercentage").asDouble(10.0);
                    fact.setDiscountPercentage(discount);
                }
            }
        } catch (Exception e) {
            log.error("Failed to apply reward for rule {}", rule.getId(), e);
        }
    }
}
