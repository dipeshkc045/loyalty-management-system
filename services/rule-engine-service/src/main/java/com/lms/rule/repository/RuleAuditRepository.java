package com.lms.rule.repository;

import com.lms.rule.model.RuleAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RuleAuditRepository extends JpaRepository<RuleAudit, Long> {
}
