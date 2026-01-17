package com.lms.rule.model;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MemberActivityFact {
    private Long memberId;

    // Monthly stats
    private long monthlyTransactionCount;
    private BigDecimal monthlyTotalSpent;

    // Quarterly stats
    private long quarterlyTransactionCount;
    private BigDecimal quarterlyTotalSpent;

    // Legacy fields (optional, for backward compatibility)
    private long transactionCount;
    private BigDecimal totalSpent;
}
