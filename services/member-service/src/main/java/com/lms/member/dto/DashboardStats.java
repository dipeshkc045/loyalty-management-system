package com.lms.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    private Long totalMembers;
    private Long totalPoints;
    private Map<String, Long> tierCounts;
    private Long platinumCount;
    private Long diamondCount;
    private Long goldCount;
    private Long silverCount;
    private Long bronzeCount;
}
