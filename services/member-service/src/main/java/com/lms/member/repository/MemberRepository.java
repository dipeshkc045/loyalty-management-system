package com.lms.member.repository;

import com.lms.member.model.Member;
import com.lms.member.model.MemberTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, JpaSpecificationExecutor<Member> {
    Optional<Member> findByEmail(String email);

    @Query("SELECT SUM(m.totalPoints) FROM Member m")
    Long sumTotalPoints();

    @Query("SELECT m.tier as tier, COUNT(m) as count FROM Member m GROUP BY m.tier")
    List<TierCountProjection> countByTier();

    default Map<String, Long> countByTierGrouped() {
        return countByTier().stream()
                .collect(Collectors.toMap(
                        p -> p.getTier().name(),
                        TierCountProjection::getCount));
    }

    interface TierCountProjection {
        MemberTier getTier();

        Long getCount();
    }
}
