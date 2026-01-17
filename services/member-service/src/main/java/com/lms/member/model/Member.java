package com.lms.member.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "members", indexes = {
        @Index(name = "idx_email", columnList = "email", unique = true),
        @Index(name = "idx_tier", columnList = "tier"),
        @Index(name = "idx_total_points", columnList = "totalPoints"),
        @Index(name = "idx_created_at", columnList = "createdAt"),
        @Index(name = "idx_tier_points", columnList = "tier, totalPoints")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String name;
    private String phone;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private MemberTier tier = MemberTier.BRONZE;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private MemberRole role = MemberRole.CUSTOMER;

    @Builder.Default
    private Integer totalPoints = 0;
    @Builder.Default
    private Integer lifetimePoints = 0;
    @Builder.Default
    private Integer expiredPoints = 0;

    private LocalDate lastTierEvaluationDate;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime updatedAt;
}
