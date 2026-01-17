package com.lms.reward.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSummary {
    private Long memberId;
    private int transactionCount;
    private BigDecimal totalAmount;
}
