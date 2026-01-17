package com.lms.reward.client;

import com.lms.reward.model.TransactionFact;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class RuleClient {
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String RULE_ENGINE_URL = "http://localhost:8083/api/v1/rules/evaluate";

    @io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker(name = "ruleEngine", fallbackMethod = "evaluateRulesFallback")
    public com.lms.reward.model.TransactionFact evaluateRules(com.lms.reward.model.RuleEvaluationRequest request) {
        return restTemplate.postForObject(RULE_ENGINE_URL, request, com.lms.reward.model.TransactionFact.class);
    }

    public com.lms.reward.model.TransactionFact evaluateRulesFallback(
            com.lms.reward.model.RuleEvaluationRequest request, Throwable t) {
        System.err.println(
                "Circuit Breaker triggered: returning default factor for " + request.getTransaction().getMemberId());
        return request.getTransaction(); // Return original fact without modifications
    }
}
