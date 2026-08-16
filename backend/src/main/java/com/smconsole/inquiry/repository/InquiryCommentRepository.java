package com.smconsole.inquiry.repository;

import com.smconsole.inquiry.entity.InquiryComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface InquiryCommentRepository extends JpaRepository<InquiryComment, Long> {

    // JOIN FETCH로 admin을 같이 가져와서, 댓글마다 작성자 조회 쿼리가 따로 나가는 N+1을 막는다.
    @Query("SELECT c FROM InquiryComment c LEFT JOIN FETCH c.admin WHERE c.inquiry.id = :inquiryId")
    List<InquiryComment> findByInquiryId(@Param("inquiryId") Long inquiryId);
}
