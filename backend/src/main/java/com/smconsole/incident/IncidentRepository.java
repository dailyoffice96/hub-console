package com.smconsole.incident;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    // 목록 조회는 필터 종류와 관계없이 reporter를 fetch join해서 N+1을 피한다.
    // (필터 없을 때만 fetch join하고 나머지는 파생 쿼리로 남겨두면, 필터를 걸 때마다
    //  toResponse()에서 incident.getReporter() 지연 로딩이 페이지 크기만큼 터진다.)
    @Query("SELECT i FROM Incident i LEFT JOIN FETCH i.reporter")
    Page<Incident> findAllFetch(Pageable pageable);

    @Query("SELECT i FROM Incident i LEFT JOIN FETCH i.reporter r WHERE r.name LIKE %:reporterName%")
    Page<Incident> findByReporter_NameContaining(@Param("reporterName") String reporterName, Pageable pageable);

    @Query("SELECT i FROM Incident i LEFT JOIN FETCH i.reporter WHERE i.status = :status")
    Page<Incident> findByStatus(@Param("status") IncidentStatus status, Pageable pageable);

    @Query("SELECT i FROM Incident i LEFT JOIN FETCH i.reporter WHERE i.severity = :severity")
    Page<Incident> findBySeverity(@Param("severity") IncidentSeverity severity, Pageable pageable);

    long countByStatus(IncidentStatus status);
    long countBySeverity(IncidentSeverity status);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);   // ← 이 줄 추가
}
