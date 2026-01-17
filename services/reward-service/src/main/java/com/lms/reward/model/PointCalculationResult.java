package com.lms.reward.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PointCalculationResult {
    private Integer basePoints;
    private Integer finalPoints;
    private Double multiplier;
}
