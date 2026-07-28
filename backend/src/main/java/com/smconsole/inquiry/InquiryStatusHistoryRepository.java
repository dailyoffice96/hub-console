package com.smconsole.inquiry;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InquiryStatusHistoryRepository extends JpaRepository<InquiryStatusHistory, Long> {

    List<InquiryStatusHistory> findByInquiryId(Long inquiryId);

}
