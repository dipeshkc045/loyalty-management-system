package com.lms.rule.model;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransactionFact {
    private Long memberId;
    private BigDecimal amount;
    private String paymentMethod;
    private String productCategory;
    private String memberTier;
    private String role; // CUSTOMER, MERCHANT

    // Output
    private double pointMultiplier = 1.0;
    private int bonusPoints = 0;
    private String rewardType = "POINTS"; // POINTS, DISCOUNT
    private double discountPercentage = 0.0;
}
