package com.lms.member.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "point_expiration_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointExpirationConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer expirationMonths; // Number of months before points expire

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpirationType expirationType;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    private String description;

    public enum ExpirationType {
        ROLLING, // Each point expires X months after it was earned
        FIXED_DATE // All points expire at a fixed date (e.g., end of year)
    }
}
