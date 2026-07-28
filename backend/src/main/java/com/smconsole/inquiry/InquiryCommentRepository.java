package com.smconsole.inquiry;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InquiryCommentRepository extends JpaRepository<InquiryComment, Long> {

    //하나의 InquiryId에 댓글은 몇 개든 있을 수 있음
    List<InquiryComment> findByInquiryId(Long inquiryId);
}
