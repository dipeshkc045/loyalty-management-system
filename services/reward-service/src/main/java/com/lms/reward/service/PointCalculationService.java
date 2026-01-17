package com.lms.reward.service;

import com.lms.reward.model.PointCalculationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
@Slf4j
public class PointCalculationService {

    public PointCalculationResult calculatePoints(BigDecimal amount, double tierMultiplier, double productMultiplier,
            double promoMultiplier) {
        BigDecimal basePoints = amount.multiply(BigDecimal.ONE); // Base rate: 1 point per 1 unit

        double finalMultiplier = tierMultiplier * productMultiplier * promoMultiplier;
        BigDecimal finalPoints = basePoints.multiply(BigDecimal.valueOf(finalMultiplier));

        return PointCalculationResult.builder()
                .basePoints(basePoints.intValue())
                .finalPoints(finalPoints.intValue())
                .multiplier(finalMultiplier)
                .build();
    }
}
