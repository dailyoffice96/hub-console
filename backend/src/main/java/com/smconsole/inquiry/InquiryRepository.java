package com.smconsole.inquiry;

import com.smconsole.incident.Incident;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    Optional<Inquiry> findByIdAndVersion(Long id, Long version);

    @Query("SELECT i FROM Inquiry i LEFT JOIN FETCH i.user LEFT JOIN FETCH i.assignee")
    Page<Inquiry> findAllFetch(Pageable pageable);

    Page<Inquiry> findByStatus(InquiryStatus inquiryStatus, Pageable pageable);
    Page<Inquiry> findByType(InquiryType type, Pageable pageable);
    Page<Inquiry> findByAssigneeName(String assigneeName, Pageable pageable);

    long countByStatus(InquiryStatus status);

}

