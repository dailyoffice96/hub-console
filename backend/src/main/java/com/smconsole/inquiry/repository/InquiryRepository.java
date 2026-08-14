package com.smconsole.inquiry.repository;

import com.smconsole.inquiry.entity.Inquiry;
import com.smconsole.inquiry.enums.InquiryStatus;
import com.smconsole.inquiry.enums.InquiryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    Optional<Inquiry> findByIdAndVersion(Long id, Long version);

    @Query("SELECT i FROM Inquiry i LEFT JOIN FETCH i.user LEFT JOIN FETCH i.assignee")
    Page<Inquiry> findAllFetch(Pageable pageable);

    // Inquiry.user/assignee는 @ManyToOne 기본값(EAGER)이라, JOIN FETCH 없이 그냥 findByXxx로 두면
    // 목록 쿼리 1번 + 행마다 user/assignee 추가 쿼리가 붙는 N+1이 발생한다. findAllFetch와 똑같이 맞춘다.
    @Query("SELECT i FROM Inquiry i LEFT JOIN FETCH i.user LEFT JOIN FETCH i.assignee WHERE i.status = :status")
    Page<Inquiry> findByStatus(@Param("status") InquiryStatus status, Pageable pageable);

    @Query("SELECT i FROM Inquiry i LEFT JOIN FETCH i.user LEFT JOIN FETCH i.assignee WHERE i.type = :type")
    Page<Inquiry> findByType(@Param("type") InquiryType type, Pageable pageable);

    @Query("SELECT i FROM Inquiry i LEFT JOIN FETCH i.user LEFT JOIN FETCH i.assignee WHERE i.assignee.name = :assigneeName")
    Page<Inquiry> findByAssigneeName(@Param("assigneeName") String assigneeName, Pageable pageable);

    long countByStatus(InquiryStatus status);
    long countByCreatedAtBetween(LocalDate start, LocalDate end);   // ← 이 줄 추가

}

