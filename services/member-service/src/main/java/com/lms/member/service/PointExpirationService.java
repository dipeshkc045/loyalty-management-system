package com.lms.member.service;

import com.lms.member.model.Member;
import com.lms.member.model.PointExpirationConfig;
import com.lms.member.model.PointTransaction;
import com.lms.member.repository.MemberRepository;
import com.lms.member.repository.PointExpirationConfigRepository;
import com.lms.member.repository.PointTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PointExpirationService {

    private final PointTransactionRepository pointTransactionRepository;
    private final MemberRepository memberRepository;
    private final PointExpirationConfigRepository configRepository;

    @Transactional
    public void expirePoints() {
        log.info("Starting point expiration process");

        LocalDateTime now = LocalDateTime.now();
        List<PointTransaction> expiredPoints = pointTransactionRepository.findExpiredPoints(now);

        log.info("Found {} expired point transactions", expiredPoints.size());

        for (PointTransaction pt : expiredPoints) {
            expirePointTransaction(pt);
        }

        log.info("Completed point expiration process");
    }

    @Transactional
    public void expirePointTransaction(PointTransaction pointTransaction) {
        try {
            // Mark as expired
            pointTransaction.setStatus(PointTransaction.PointStatus.EXPIRED);
            pointTransactionRepository.save(pointTransaction);

            // Deduct from member's total points
            Member member = memberRepository.findById(pointTransaction.getMemberId())
                    .orElseThrow(() -> new RuntimeException("Member not found"));

            int currentPoints = member.getTotalPoints();
            int newPoints = Math.max(0, currentPoints - pointTransaction.getPoints());

            member.setTotalPoints(newPoints);
            member.setExpiredPoints(member.getExpiredPoints() + pointTransaction.getPoints());
            memberRepository.save(member);

            log.info("Expired {} points for member {}. New balance: {}",
                    pointTransaction.getPoints(), member.getId(), newPoints);

        } catch (Exception e) {
            log.error("Failed to expire point transaction {}", pointTransaction.getId(), e);
        }
    }

    public LocalDateTime calculateExpiryDate(LocalDateTime earnedDate) {
        PointExpirationConfig config = configRepository.findFirstByIsActiveTrueOrderByIdDesc()
                .orElse(PointExpirationConfig.builder()
                        .expirationMonths(12)
                        .expirationType(PointExpirationConfig.ExpirationType.ROLLING)
                        .build());

        return earnedDate.plusMonths(config.getExpirationMonths());
    }
}
