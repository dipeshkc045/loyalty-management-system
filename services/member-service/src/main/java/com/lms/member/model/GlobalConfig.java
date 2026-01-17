package com.lms.member.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "global_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlobalConfig {
    @Id
    private String configKey;
    private String configValue;
}
