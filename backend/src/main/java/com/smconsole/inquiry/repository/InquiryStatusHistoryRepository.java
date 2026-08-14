package com.smconsole.inquiry.repository;

import com.smconsole.inquiry.entity.InquiryStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface InquiryStatusHistoryRepository extends JpaRepository<InquiryStatusHistory, Long> {

    //JOIN FETCH로 changedBy를 같이 가져와서, 이력 행마다 변경자 조회 쿼리가 따로 나가는 N+1을 막는다.
    @Query("SELECT h FROM InquiryStatusHistory h LEFT JOIN FETCH h.changedBy WHERE h.inquiry.id = :inquiryId")
    List<InquiryStatusHistory> findByInquiryId(@Param("inquiryId") Long inquiryId);

}
