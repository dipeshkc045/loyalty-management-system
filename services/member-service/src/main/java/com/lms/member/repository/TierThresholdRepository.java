package com.lms.member.repository;

import com.lms.member.model.TierThreshold;
import com.lms.member.model.MemberTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TierThresholdRepository extends JpaRepository<TierThreshold, Long> {
    Optional<TierThreshold> findByTargetTier(MemberTier tier);

    List<TierThreshold> findAllByOrderByPriorityAsc();
}
