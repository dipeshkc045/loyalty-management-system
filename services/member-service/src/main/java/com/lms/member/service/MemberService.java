package com.lms.member.service;

import com.lms.member.dto.DashboardStats;
import com.lms.member.dto.MemberDTO;
import com.lms.member.model.Member;
import com.lms.member.model.MemberTier;
import com.lms.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final com.lms.member.repository.ProcessedTransactionRepository processedTransactionRepository;
    private final PointExpirationService pointExpirationService;
    private final com.lms.member.repository.PointTransactionRepository pointTransactionRepository;

    @Transactional
    public Member createMember(Member member) {
        if (memberRepository.findByEmail(member.getEmail()).isPresent()) {
            throw new RuntimeException("Member with email " + member.getEmail() + " already exists");
        }
        return memberRepository.save(member);
    }

    @Cacheable(value = "members", key = "#id")
    public Optional<Member> getMember(Long id) {
        return memberRepository.findById(id);
    }

    // New: Get all members with pagination and filtering
    public Page<MemberDTO> getAllMembers(Pageable pageable, String search, MemberTier tier) {
        Specification<Member> spec = buildSearchSpec(search, tier);
        Page<Member> memberPage = memberRepository.findAll(spec, pageable);
        return memberPage.map(this::toDTO);
    }

    // New: Get dashboard statistics
    @Cacheable(value = "dashboard-stats")
    public DashboardStats getDashboardStats() {
        Long totalMembers = memberRepository.count();
        Long totalPoints = memberRepository.sumTotalPoints();
        if (totalPoints == null)
            totalPoints = 0L;

        Map<String, Long> tierCounts = memberRepository.countByTierGrouped();

        return DashboardStats.builder()
                .totalMembers(totalMembers)
                .totalPoints(totalPoints)
                .tierCounts(tierCounts)
                .platinumCount(tierCounts.getOrDefault("PLATINUM", 0L))
                .diamondCount(tierCounts.getOrDefault("DIAMOND", 0L))
                .goldCount(tierCounts.getOrDefault("GOLD", 0L))
                .silverCount(tierCounts.getOrDefault("SILVER", 0L))
                .bronzeCount(tierCounts.getOrDefault("BRONZE", 0L))
                .build();
    }

    // New: Get lightweight member list for dropdowns
    @Cacheable(value = "members-lite")
    public List<MemberDTO> getMembersLite(int limit) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit,
                org.springframework.data.domain.Sort.by("id").descending());
        return memberRepository.findAll(pageable).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = { "members", "dashboard-stats", "members-lite" }, allEntries = true)
    public Member updateMember(Long id, Member memberDetails) {
        return memberRepository.findById(id).map(member -> {
            member.setName(memberDetails.getName());
            member.setPhone(memberDetails.getPhone());
            return memberRepository.save(member);
        }).orElseThrow(() -> new RuntimeException("Member not found"));
    }

    @Transactional
    @CacheEvict(value = { "members", "dashboard-stats", "members-lite" }, allEntries = true)
    public void awardPoints(Long memberId, Integer points, Long transactionId) {
        if (processedTransactionRepository.findByTransactionId(transactionId).isPresent()) {
            System.err.println("Transaction " + transactionId + " already processed. Skipping.");
            return;
        }

        memberRepository.findById(memberId).ifPresent(member -> {
            member.setTotalPoints(member.getTotalPoints() + points);
            member.setLifetimePoints(member.getLifetimePoints() + points);

            // Tier upgrade logic
            updateTier(member);

            memberRepository.save(member);

            // Create point transaction record with expiry date
            java.time.LocalDateTime earnedDate = java.time.LocalDateTime.now();
            java.time.LocalDateTime expiryDate = pointExpirationService.calculateExpiryDate(earnedDate);

            pointTransactionRepository.save(com.lms.member.model.PointTransaction.builder()
                    .memberId(memberId)
                    .points(points)
                    .earnedDate(earnedDate)
                    .expiryDate(expiryDate)
                    .status(com.lms.member.model.PointTransaction.PointStatus.ACTIVE)
                    .transactionId(transactionId)
                    .reason("Transaction reward")
                    .build());

            processedTransactionRepository.save(com.lms.member.model.ProcessedTransaction.builder()
                    .transactionId(transactionId)
                    .build());
        });
    }

    private void updateTier(com.lms.member.model.Member member) {
        int points = member.getLifetimePoints();
        if (points >= 50000)
            member.setTier(com.lms.member.model.MemberTier.DIAMOND);
        else if (points >= 15000)
            member.setTier(com.lms.member.model.MemberTier.PLATINUM);
        else if (points >= 5000)
            member.setTier(com.lms.member.model.MemberTier.GOLD);
        else if (points >= 1000)
            member.setTier(com.lms.member.model.MemberTier.SILVER);
    }

    public Integer getPointBalance(Long id) {
        return memberRepository.findById(id)
                .map(Member::getTotalPoints)
                .orElseThrow(() -> new RuntimeException("Member not found"));
    }

    // Helper: Build search specification
    private Specification<Member> buildSearchSpec(String search, MemberTier tier) {
        Specification<Member> spec = Specification.where(null);

        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase();
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), "%" + searchLower + "%"),
                    cb.like(cb.lower(root.get("email")), "%" + searchLower + "%"),
                    cb.like(cb.lower(root.get("phone")), "%" + searchLower + "%")));
        }

        if (tier != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("tier"), tier));
        }

        return spec;
    }

    // Helper: Convert Member to MemberDTO
    private MemberDTO toDTO(Member member) {
        return MemberDTO.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .phone(member.getPhone())
                .tier(member.getTier())
                .totalPoints(member.getTotalPoints())
                .build();
    }

    @Transactional
    @CacheEvict(value = { "members", "dashboard-stats", "members-lite" }, allEntries = true)
    public void deleteMember(Long id) {
        if (!memberRepository.existsById(id)) {
            throw new RuntimeException("Member not found");
        }
        memberRepository.deleteById(id);
    }

    @Transactional
    @CacheEvict(value = { "members", "dashboard-stats", "members-lite" }, allEntries = true)
    public void resetMemberPoints(Long id) {
        // Delete all point history
        pointTransactionRepository.deleteByMemberId(id);

        // Reset Member
        memberRepository.findById(id).ifPresent(member -> {
            member.setTotalPoints(500);
            member.setLifetimePoints(500);
            member.setTier(MemberTier.BRONZE);
            memberRepository.save(member);

            // Add Onboarding Points entry
            java.time.LocalDateTime earnedDate = java.time.LocalDateTime.now();
            java.time.LocalDateTime expiryDate = pointExpirationService.calculateExpiryDate(earnedDate);

            pointTransactionRepository.save(com.lms.member.model.PointTransaction.builder()
                    .memberId(id)
                    .points(500)
                    .earnedDate(earnedDate)
                    .expiryDate(expiryDate)
                    .status(com.lms.member.model.PointTransaction.PointStatus.ACTIVE)
                    .reason("Welcome Bonus")
                    .build());
        });
    }
}
