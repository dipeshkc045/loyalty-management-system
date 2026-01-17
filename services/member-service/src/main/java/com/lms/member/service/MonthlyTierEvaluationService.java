package com.lms.member.service;

import com.lms.member.model.Member;
import com.lms.member.model.MemberTier;
import com.lms.member.model.TierThreshold;
import com.lms.member.repository.MemberRepository;
import com.lms.member.repository.TierThresholdRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonthlyTierEvaluationService {

    private final MemberRepository memberRepository;
    private final TierThresholdRepository tierThresholdRepository;
    private final com.lms.member.client.TransactionClient transactionClient;

    @Transactional
    public void evaluateAllMemberTiers() {
        log.info("Starting monthly tier evaluation for all members");

        List<Member> members = memberRepository.findAll();
        List<TierThreshold> thresholds = tierThresholdRepository.findAllByOrderByPriorityAsc();

        YearMonth currentMonth = YearMonth.now();

        for (Member member : members) {
            evaluateMemberTier(member, thresholds, currentMonth);
        }

        log.info("Completed monthly tier evaluation for {} members", members.size());
    }

    @Transactional
    public void evaluateMemberTier(Member member, List<TierThreshold> thresholds, YearMonth month) {
        try {
            // Get monthly transaction summary from transaction service
            var summary = transactionClient.getMonthlySummary(member.getId(), month.toString());

            BigDecimal monthlyAmount = summary.getTotalAmount();
            Integer monthlyTransactionCount = summary.getTransactionCount();

            log.info("Member {}: Monthly amount={}, Transaction count={}",
                    member.getId(), monthlyAmount, monthlyTransactionCount);

            // Determine the highest tier the member qualifies for
            MemberTier qualifiedTier = MemberTier.BRONZE; // Default

            for (TierThreshold threshold : thresholds) {
                // Both criteria must be met (AND logic)
                if (monthlyAmount.compareTo(threshold.getMinMonthlyAmount()) >= 0 &&
                        monthlyTransactionCount >= threshold.getMinMonthlyTransactionCount()) {
                    qualifiedTier = threshold.getTargetTier();
                }
            }

            // Update tier if changed
            if (member.getTier() != qualifiedTier) {
                MemberTier oldTier = member.getTier();
                member.setTier(qualifiedTier);
                member.setLastTierEvaluationDate(LocalDate.now());
                memberRepository.save(member);

                log.info("Member {} tier changed: {} -> {}", member.getId(), oldTier, qualifiedTier);
            } else {
                member.setLastTierEvaluationDate(LocalDate.now());
                memberRepository.save(member);
                log.info("Member {} tier unchanged: {}", member.getId(), qualifiedTier);
            }

        } catch (Exception e) {
            log.error("Failed to evaluate tier for member {}", member.getId(), e);
        }
    }
}
