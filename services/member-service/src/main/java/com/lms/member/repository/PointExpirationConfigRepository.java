package com.lms.member.repository;

import com.lms.member.model.PointExpirationConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PointExpirationConfigRepository extends JpaRepository<PointExpirationConfig, Long> {
    Optional<PointExpirationConfig> findFirstByIsActiveTrueOrderByIdDesc();
}
