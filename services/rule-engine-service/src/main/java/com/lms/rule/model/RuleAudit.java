package com.lms.rule.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "rule_audits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;
    private String ruleName;
    private String resultType; // e.g. MULTIPLIER, BONUS_POINTS
    private String resultValue;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
