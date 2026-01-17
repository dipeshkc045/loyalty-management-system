package com.lms.member.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "point_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Integer points;

    @Column(nullable = false)
    private LocalDateTime earnedDate;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointStatus status;

    private Long transactionId; // Reference to the transaction that earned these points

    private String reason;

    public enum PointStatus {
        ACTIVE,
        EXPIRED,
        REDEEMED
    }
}
