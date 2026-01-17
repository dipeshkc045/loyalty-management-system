package com.lms.member.controller;

import com.lms.member.model.PointExpirationConfig;
import com.lms.member.model.TierThreshold;
import com.lms.member.repository.PointExpirationConfigRepository;
import com.lms.member.repository.TierThresholdRepository;
import com.lms.member.service.MonthlyTierEvaluationService;
import com.lms.member.service.PointExpirationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
@Tag(name = "Configuration", description = "APIs for managing point expiration and tier threshold configurations")
public class ConfigurationController {

    private final TierThresholdRepository tierThresholdRepository;
    private final PointExpirationConfigRepository pointExpirationConfigRepository;
    private final MonthlyTierEvaluationService tierEvaluationService;
    private final PointExpirationService pointExpirationService;

    @GetMapping("/tiers")
    @Operation(summary = "Get all tier thresholds")
    public ResponseEntity<List<TierThreshold>> getTierThresholds() {
        return ResponseEntity.ok(tierThresholdRepository.findAllByOrderByPriorityAsc());
    }

    @PostMapping("/tiers")
    @Operation(summary = "Create or update a tier threshold")
    public ResponseEntity<TierThreshold> saveTierThreshold(@RequestBody TierThreshold threshold) {
        return ResponseEntity.ok(tierThresholdRepository.save(threshold));
    }

    @GetMapping("/expiration")
    @Operation(summary = "Get active point expiration config")
    public ResponseEntity<PointExpirationConfig> getExpirationConfig() {
        return pointExpirationConfigRepository.findFirstByIsActiveTrueOrderByIdDesc()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/expiration")
    @Operation(summary = "Update point expiration config")
    public ResponseEntity<PointExpirationConfig> updateExpirationConfig(@RequestBody PointExpirationConfig config) {
        // Deactivate previous configs if needed, or just insert new one which will be
        // picked up by "findFirstByOrderByIdDesc"
        // For simplicity, we just save the new one
        return ResponseEntity.ok(pointExpirationConfigRepository.save(config));
    }

    @PostMapping("/admin/run-tier-evaluation")
    @Operation(summary = "Manually trigger monthly tier evaluation")
    public ResponseEntity<String> runTierEvaluation() {
        tierEvaluationService.evaluateAllMemberTiers();
        return ResponseEntity.ok("Tier evaluation triggered successfully");
    }

    @PostMapping("/admin/expire-points")
    @Operation(summary = "Manually trigger point expiration")
    public ResponseEntity<String> expirePoints() {
        pointExpirationService.expirePoints();
        return ResponseEntity.ok("Point expiration triggered successfully");
    }
}
