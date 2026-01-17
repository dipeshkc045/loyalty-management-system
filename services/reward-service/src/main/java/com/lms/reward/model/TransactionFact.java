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
public class TransactionFact {
    private Long memberId;
    private BigDecimal amount;
    private String paymentMethod;
    private String productCategory;
    private String memberTier;
    private String role;

    @Builder.Default
    private double pointMultiplier = 1.0;
    @Builder.Default
    private int bonusPoints = 0;
    @Builder.Default
    private String rewardType = "POINTS";
    @Builder.Default
    private double discountPercentage = 0.0;
}
