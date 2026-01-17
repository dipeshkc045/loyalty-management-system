package com.lms.member.repository;

import com.lms.member.model.PointTransaction;
import com.lms.member.model.PointTransaction.PointStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

    List<PointTransaction> findByMemberIdAndStatus(Long memberId, PointStatus status);

    @Query("SELECT pt FROM PointTransaction pt WHERE pt.status = 'ACTIVE' AND pt.expiryDate < :now")
    List<PointTransaction> findExpiredPoints(LocalDateTime now);

    @Query("SELECT SUM(pt.points) FROM PointTransaction pt WHERE pt.memberId = :memberId AND pt.status = 'ACTIVE'")
    Integer sumActivePointsByMemberId(Long memberId);

    void deleteByMemberId(Long memberId);
}
