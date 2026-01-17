package com.lms.reward.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleEvaluationRequest {
    private TransactionFact transaction;
    private MemberActivityFact activity;
}
