package com.lms.rule.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;

@Entity
@Table(name = "rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ruleType;

    @Column(nullable = false)
    private String ruleName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode conditions;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode actions;

    @Column(columnDefinition = "text")
    private String drlContent;

    private Integer priority;

    @Builder.Default
    private Boolean isActive = true;

    // Advanced Rule Fields
    private String evaluationType; // TRANSACTION, MONTHLY, QUARTERLY
    private String targetProductCode; // Legacy field

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private java.util.List<String> targetProductCodes;

    private java.math.BigDecimal minAmount;
    private java.math.BigDecimal maxAmount;
    private Integer minVolume;
    private Integer maxVolume;
    private String targetTier; // BRONZE, SILVER, GOLD, etc.
    private String rewardType; // POINTS, DISCOUNT

    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
}
