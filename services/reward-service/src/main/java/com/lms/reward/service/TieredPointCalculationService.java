package com.lms.reward.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class TieredPointCalculationService {

    /**
     * Calculate points based on spending ranges and member tier
     * 
     * Spending Ranges and Points:
     * 0-100: Bronze=1, Silver=1, Gold=2, Platinum=3, Diamond=5
     * 101-1000: Bronze=2, Silver=2, Gold=3, Platinum=4, Diamond=6
     * 1001-5000: Bronze=3, Silver=4, Gold=5, Platinum=6, Diamond=8
     * 5001+: Bronze=5, Silver=6, Gold=8, Platinum=10, Diamond=15
     */
    public int calculatePoints(BigDecimal amount, String memberTier) {
        double amountValue = amount.doubleValue();

        // Determine spending range
        SpendingRange range = getSpendingRange(amountValue);

        // Get points based on range and tier
        int points = getPointsForRangeAndTier(range, memberTier);

        log.info("Calculated {} points for amount {} (tier: {}, range: {})",
                points, amount, memberTier, range);

        return points;
    }

    private SpendingRange getSpendingRange(double amount) {
        if (amount <= 100) {
            return SpendingRange.RANGE_0_100;
        } else if (amount <= 1000) {
            return SpendingRange.RANGE_101_1000;
        } else if (amount <= 5000) {
            return SpendingRange.RANGE_1001_5000;
        } else {
            return SpendingRange.RANGE_5001_PLUS;
        }
    }

    private int getPointsForRangeAndTier(SpendingRange range, String tier) {
        return switch (range) {
            case RANGE_0_100 -> switch (tier) {
                case "BRONZE", "SILVER" -> 1;
                case "GOLD" -> 2;
                case "PLATINUM" -> 3;
                case "DIAMOND" -> 5;
                default -> 1;
            };
            case RANGE_101_1000 -> switch (tier) {
                case "BRONZE", "SILVER" -> 2;
                case "GOLD" -> 3;
                case "PLATINUM" -> 4;
                case "DIAMOND" -> 6;
                default -> 2;
            };
            case RANGE_1001_5000 -> switch (tier) {
                case "BRONZE" -> 3;
                case "SILVER" -> 4;
                case "GOLD" -> 5;
                case "PLATINUM" -> 6;
                case "DIAMOND" -> 8;
                default -> 3;
            };
            case RANGE_5001_PLUS -> switch (tier) {
                case "BRONZE" -> 5;
                case "SILVER" -> 6;
                case "GOLD" -> 8;
                case "PLATINUM" -> 10;
                case "DIAMOND" -> 15;
                default -> 5;
            };
        };
    }

    private enum SpendingRange {
        RANGE_0_100,
        RANGE_101_1000,
        RANGE_1001_5000,
        RANGE_5001_PLUS
    }
}
