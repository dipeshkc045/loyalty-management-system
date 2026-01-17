package com.lms.rule.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.rule.model.Rule;
import com.lms.rule.repository.RuleRepository;
import com.lms.rule.service.RuleLoaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RuleSeeder implements CommandLineRunner {

        private final RuleRepository ruleRepository;
        private final RuleLoaderService ruleLoaderService;
        private final ObjectMapper objectMapper;

        @Override
        public void run(String... args) throws Exception {
                if (ruleRepository.count() == 0) {
                        System.out.println("Seeding default rules...");

                        // Onboarding Rule
                        Rule onboardingRule = Rule.builder()
                                        .ruleType("EVENT")
                                        .ruleName("Welcome Bonus")
                                        .conditions(objectMapper.readTree("{\"eventType\": \"ONBOARDING\"}"))
                                        .actions(objectMapper
                                                        .readTree("{\"points\": 500, \"reason\": \"Welcome Bonus\"}"))
                                        .priority(1)
                                        .isActive(true)
                                        .build();
                        ruleRepository.save(onboardingRule);

                        // Referral Rule
                        Rule referralRule = Rule.builder()
                                        .ruleType("EVENT")
                                        .ruleName("Referral Bonus")
                                        .conditions(objectMapper.readTree("{\"eventType\": \"REFERRAL\"}"))
                                        .actions(objectMapper.readTree(
                                                        "{\"referrerPoints\": 1000, \"refereePoints\": 500, \"reason\": \"Referral Bonus\"}"))
                                        .priority(1)
                                        .isActive(true)
                                        .build();
                        ruleRepository.save(referralRule);

                        // Tiered Transaction Rule
                        Rule tieredTxRule = Rule.builder()
                                        .ruleType("TRANSACTION")
                                        .ruleName("Tiered Spending Bonus")
                                        .priority(2)
                                        .isActive(true)
                                        .evaluationType("TRANSACTION")
                                        .rewardType("POINTS")
                                        .actions(objectMapper.readTree(
                                                        "[{\"type\": \"TIERED_POINTS\", \"ranges\": [" +
                                                                        "{\"min\": 0, \"max\": 100, \"points\": 50, \"multiplier\": 0}, "
                                                                        +
                                                                        "{\"min\": 101, \"max\": 500, \"points\": 100, \"multiplier\": 0.1}, "
                                                                        +
                                                                        "{\"min\": 501, \"points\": 500, \"multiplier\": 0.2}"
                                                                        +
                                                                        "]}]"))
                                        .build();
                        ruleRepository.save(tieredTxRule);

                        System.out.println("Default rules seeded.");

                        // Rebuild Drools container to include new rules
                        ruleLoaderService.rebuildContainer();
                }

                // Always seed Tiered Spending Bonus if not present
                if (!ruleRepository.findAll().stream().anyMatch(r -> "Tiered Spending Bonus".equals(r.getRuleName()))) {
                        Rule tieredTxRule = Rule.builder()
                                        .ruleType("TRANSACTION")
                                        .ruleName("Tiered Spending Bonus")
                                        .priority(2)
                                        .isActive(true)
                                        .evaluationType("TRANSACTION")
                                        .rewardType("POINTS")
                                        .actions(objectMapper.readTree(
                                                        "[{\"type\": \"TIERED_POINTS\", \"ranges\": [" +
                                                                        "{\"min\": 0, \"max\": 100, \"points\": 50, \"multiplier\": 0}, "
                                                                        +
                                                                        "{\"min\": 101, \"max\": 500, \"points\": 100, \"multiplier\": 0.1}, "
                                                                        +
                                                                        "{\"min\": 501, \"points\": 500, \"multiplier\": 0.2}"
                                                                        +
                                                                        "]}]"))
                                        .build();
                        ruleRepository.save(tieredTxRule);
                        ruleLoaderService.rebuildContainer();
                        System.out.println("Tiered Spending Bonus rule seeded.");
                }
        }
}
