package com.lms.rule.service;

import com.lms.rule.model.Rule;
import com.lms.rule.repository.RuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.runtime.KieContainer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleLoaderService {
    private final RuleRepository ruleRepository;
    private final KieServices kieServices = KieServices.Factory.get();
    private KieContainer kieContainer;

    public KieContainer getKieContainer() {
        if (kieContainer == null) {
            rebuildContainer();
        }
        return kieContainer;
    }

    public synchronized void rebuildContainer() {
        log.info("Rebuilding KieContainer from database rules...");
        List<Rule> activeRules = ruleRepository.findAll().stream()
                .filter(Rule::getIsActive)
                .filter(r -> r.getDrlContent() != null && !r.getDrlContent().isEmpty())
                .collect(Collectors.toList());

        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

        // Add default rules if needed or just load all from DB
        for (Rule rule : activeRules) {
            String path = "src/main/resources/rules/" + rule.getRuleName().replaceAll("\\s+", "_") + ".drl";
            kieFileSystem.write(path, rule.getDrlContent());
        }

        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();

        if (kieBuilder.getResults().hasMessages(org.kie.api.builder.Message.Level.ERROR)) {
            log.error("Error building Drools rules: {}", kieBuilder.getResults().getMessages());
            throw new RuntimeException("Build Errors occurred during rule compilation");
        }

        KieModule kieModule = kieBuilder.getKieModule();
        this.kieContainer = kieServices.newKieContainer(kieModule.getReleaseId());
        log.info("KieContainer rebuilt successfully with {} rules.", activeRules.size());
    }
}
