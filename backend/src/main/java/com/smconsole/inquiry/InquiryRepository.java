package com.smconsole.inquiry;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    @Query("SELECT i FROM Inquiry i JOIN FETCH i.user JOIN FETCH i.assignee")
    Page<Inquiry> findAllFetch(Pageable pageable);

    Page<Inquiry> findByStatus(InquiryStatus inquiryStatus, Pageable pageable);
    Page<Inquiry> findByType(InquiryType type, Pageable pageable);
    Page<Inquiry> findByAssigneeName(String assigneeName, Pageable pageable);
}

