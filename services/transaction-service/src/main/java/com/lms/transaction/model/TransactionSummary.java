package com.lms.transaction.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TransactionSummary {
    private Long memberId;
    private long transactionCount;
    private BigDecimal totalAmount;
}
