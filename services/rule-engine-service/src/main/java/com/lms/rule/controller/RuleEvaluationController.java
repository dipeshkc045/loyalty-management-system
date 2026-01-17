package com.lms.rule.controller;

import com.lms.rule.model.TransactionFact;
import com.lms.rule.service.RuleMatcherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rules")
@RequiredArgsConstructor
public class RuleEvaluationController {
    private final RuleMatcherService ruleMatcherService;

    @PostMapping("/evaluate")
    public ResponseEntity<TransactionFact> evaluate(@RequestBody com.lms.rule.model.RuleEvaluationRequest request) {
        return ResponseEntity.ok(ruleMatcherService.evaluateRules(request.getTransaction(), request.getActivity()));
    }
}
