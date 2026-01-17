package com.lms.reward.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointsEarnedEvent {
    private Long memberId;
    private Long transactionId;
    private Integer pointsEarned;
    private String reason;
}
