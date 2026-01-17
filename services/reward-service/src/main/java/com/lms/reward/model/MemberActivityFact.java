package com.lms.reward.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberActivityFact {
    private Long memberId;

    // Monthly stats
    private long monthlyTransactionCount;
    private BigDecimal monthlyTotalSpent;

    // Quarterly stats
    private long quarterlyTransactionCount;
    private BigDecimal quarterlyTotalSpent;

    // Legacy fields
    private long transactionCount;
    private BigDecimal totalSpent;
}
