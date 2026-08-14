package com.smconsole.auditlog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("SELECT a FROM AuditLog a LEFT JOIN FETCH a.admin ORDER BY a.createdAt DESC")
    Page<AuditLog> findAllFetch(Pageable pageable);

    // 엑셀 전체 내보내기용 - 페이지네이션 없이 전량을 admin fetch join과 함께 가져온다.
    @Query("SELECT a FROM AuditLog a LEFT JOIN FETCH a.admin ORDER BY a.createdAt DESC")
    List<AuditLog> findAllFetchAsList();

    // 필터가 걸리는 조회도 findAllFetch()와 마찬가지로 admin을 fetch join해야 한다.
    // 그렇지 않으면 toResponse()의 auditLog.getAdmin() 호출이 페이지 크기만큼 지연 로딩(N+1)을 유발한다.
    @Query("SELECT a FROM AuditLog a LEFT JOIN FETCH a.admin r WHERE r.name LIKE %:adminName% ORDER BY a.createdAt DESC")
    Page<AuditLog> findByAdmin_NameContaining(@Param("adminName") String adminName, Pageable pageable);

    @Query("SELECT a FROM AuditLog a LEFT JOIN FETCH a.admin WHERE a.targetType = :targetType ORDER BY a.createdAt DESC")
    Page<AuditLog> findByTargetType(@Param("targetType") AuditTargetType targetType, Pageable pageable);

    @Query("SELECT a FROM AuditLog a LEFT JOIN FETCH a.admin WHERE a.action = :action ORDER BY a.createdAt DESC")
    Page<AuditLog> findByAction(@Param("action") AuditAction action, Pageable pageable);

    List<AuditLog> findByCreatedAtAfter(LocalDateTime time);
}
