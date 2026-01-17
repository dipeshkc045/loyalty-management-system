package com.lms.rule.model;

import lombok.Data;

@Data
public class RuleEvaluationRequest {
    private TransactionFact transaction;
    private MemberActivityFact activity;
}
