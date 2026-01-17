package com.lms.member.dto;

import com.lms.member.model.MemberTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private MemberTier tier;
    private Integer totalPoints;
}
