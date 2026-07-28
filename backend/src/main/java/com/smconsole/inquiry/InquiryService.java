package com.smconsole.inquiry;

import com.smconsole.admin.AdminRepository;
import com.smconsole.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@RequiredArgsConstructor
@Transactional
public class InquiryService {
    private final InquiryCommentRepository inquiryCommentRepository;
    private final InquiryRepository inquiryRepository;
    private final InquiryStatusHistoryRepository inquiryStatusHistoryRepository;
    private final AdminRepository adminRepository;


    public Page<InquiryResponse>getInquiry(
            String assigneeName, InquiryStatus status,
            InquiryType type, Pageable pageable) {
        Page<Inquiry> inquiries;

        if (assigneeName != null) {
            inquiries = inquiryRepository.findByAssigneeName(assigneeName, pageable);
        } else if (status != null) {
            inquiries = inquiryRepository.findByStatus(status, pageable);
        } else if (type != null) {
            inquiries = inquiryRepository.findByType(type, pageable);
        } else {
            inquiries = inquiryRepository.findAll(pageable);
        }

        return inquiries.map(this::toResponse);
    }

    public InquiryDetailResponse getDetail(Long id) {
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의사항입니다."));
        return toResponse(inquiry);
    }

    public InquiryCommentResponse createComment(Long id) {
        Inquiry inquiry = inquiryCommentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        return toResponse(inquiry);
    }

    public ResponseEntity<InquiryResponse> updateStatus(@PathVariable Long id, @RequestBody InquiryStatus status){

    }

    public ResponseEntity<InquiryResponse> updatesAssign(@PathVariable Long id, @RequestBody Long adminId){

    }

}