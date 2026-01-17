package com.lms.member.scheduler;

import com.lms.member.service.MonthlyTierEvaluationService;
import com.lms.member.service.PointExpirationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MonthlyScheduler {

    private final MonthlyTierEvaluationService tierEvaluationService;
    private final PointExpirationService pointExpirationService;

    // Run at 00:00 on the 1st day of every month
    @Scheduled(cron = "0 0 0 1 * ?")
    public void runMonthlyTierEvaluation() {
        log.info("Starting scheduled monthly tier evaluation");
        tierEvaluationService.evaluateAllMemberTiers();
        log.info("Finished scheduled monthly tier evaluation");
    }

    // Run at 01:00 every day to check for expired points
    @Scheduled(cron = "0 0 1 * * ?")
    public void runDailyPointExpiration() {
        log.info("Starting scheduled daily point expiration check");
        pointExpirationService.expirePoints();
        log.info("Finished scheduled daily point expiration check");
    }
}
