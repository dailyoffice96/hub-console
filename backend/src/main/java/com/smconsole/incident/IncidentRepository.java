package com.smconsole.incident;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    Optional<Incident> findByIdAndVersion(Long id, Long version);

    @Query("SELECT i FROM Incident i LEFT JOIN FETCH i.reporter")
    Page<Incident> findAllFetch(Pageable pageable);

    Page<Incident> findByReporter_NameContaining(String reporterName, Pageable pageable);
    Page<Incident> findByStatus(IncidentStatus status, Pageable pageable);
    Page<Incident> findBySeverity(IncidentSeverity severity, Pageable pageable);

    long countByStatus(IncidentStatus status);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);   // ← 이 줄 추가
}

