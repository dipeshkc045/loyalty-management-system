package com.lms.rule.repository;

import com.lms.rule.model.Rule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RuleRepository extends JpaRepository<Rule, Long> {
    List<Rule> findByIsActiveTrueOrderByPriorityDesc();
}
