package com.lms.member.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tier_thresholds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TierThreshold {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private MemberTier targetTier;

    @Column(nullable = false)
    private BigDecimal minMonthlyAmount;

    @Column(nullable = false)
    private Integer minMonthlyTransactionCount;

    @Column(nullable = false)
    private Integer priority; // Lower number = higher priority

    private String description;
}
