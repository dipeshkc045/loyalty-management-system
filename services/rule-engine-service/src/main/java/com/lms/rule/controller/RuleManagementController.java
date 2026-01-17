package com.lms.rule.controller;

import com.lms.rule.model.Rule;
import com.lms.rule.repository.RuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rules")
@RequiredArgsConstructor
public class RuleManagementController {
    private final RuleRepository ruleRepository;

    @PostMapping
    public ResponseEntity<Rule> createRule(@RequestBody Rule rule) {
        Rule saved = ruleRepository.save(rule);
        ruleLoaderService.rebuildContainer();
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<Rule>> listRules() {
        return ResponseEntity.ok(ruleRepository.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Rule> updateRule(@PathVariable Long id, @RequestBody Rule ruleDetails) {
        return ruleRepository.findById(id).map(rule -> {
            rule.setRuleName(ruleDetails.getRuleName());
            rule.setConditions(ruleDetails.getConditions());
            rule.setActions(ruleDetails.getActions());
            rule.setIsActive(ruleDetails.getIsActive());
            rule.setEvaluationType(ruleDetails.getEvaluationType());
            rule.setTargetProductCode(ruleDetails.getTargetProductCode());
            rule.setMinAmount(ruleDetails.getMinAmount());
            rule.setMaxAmount(ruleDetails.getMaxAmount());
            rule.setMinVolume(ruleDetails.getMinVolume());
            rule.setMaxVolume(ruleDetails.getMaxVolume());
            rule.setRewardType(ruleDetails.getRewardType());
            rule.setTargetTier(ruleDetails.getTargetTier());
            rule.setPriority(ruleDetails.getPriority());
            rule.setTargetProductCodes(ruleDetails.getTargetProductCodes());

            Rule saved = ruleRepository.save(rule);
            ruleLoaderService.rebuildContainer();
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllRules() {
        ruleRepository.deleteAll();
        ruleLoaderService.rebuildContainer();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        ruleRepository.deleteById(id);
        ruleLoaderService.rebuildContainer();
        return ResponseEntity.noContent().build();
    }

    private final com.lms.rule.service.RuleLoaderService ruleLoaderService;

    @PostMapping("/reload")
    public ResponseEntity<String> reloadRules() {
        ruleLoaderService.rebuildContainer();
        return ResponseEntity.ok("Rules reloaded successfully from database.");
    }
}
