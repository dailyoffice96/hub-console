package com.smconsole.inquiry;

import com.smconsole.admin.Admin;
import com.smconsole.admin.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final InquiryCommentRepository inquiryCommentRepository;
    private final InquiryStatusHistoryRepository inquiryStatusHistoryRepository;
    private final AdminRepository adminRepository;

    // 1. 목록조회 (검색+필터+페이징)
    public Page<InquiryResponse> getInquiry(String assigneeName, InquiryStatus status, InquiryType type, Pageable pageable) {
        Page<Inquiry> inquiries;

        if (status != null) {
            inquiries = inquiryRepository.findByStatus(status, pageable);
        } else if (type != null) {
            inquiries = inquiryRepository.findByType(type, pageable);
        } else {
            inquiries = inquiryRepository.findAll(pageable);
        }

        return inquiries.map(this::toResponse);
    }

    // 2. 개별조회 (댓글+이력 포함)
    public InquiryDetailResponse getDetail(Long id) {
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의입니다."));

        List<InquiryComment> comments = inquiryCommentRepository.findByInquiryId(id);
        List<InquiryStatusHistory> histories = inquiryStatusHistoryRepository.findByInquiryId(id);

        return new InquiryDetailResponse(
                inquiry.getId(),
                inquiry.getUser().getName(),
                inquiry.getAssignee() != null ? inquiry.getAssignee().getName() : null,
                inquiry.getType(),
                inquiry.getTitle(),
                inquiry.getContent(),
                inquiry.getStatus(),
                inquiry.getCreatedAt(),
                inquiry.getCompletedAt(),
                comments.stream().map(this::toCommentResponse).toList(),
                histories.stream().map(this::toHistoryResponse).toList()
        );
    }

    // 3. 댓글 작성
    public InquiryCommentResponse createComment(Long inquiryId, String content) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의입니다."));

        // 임시로 첫 번째 관리자를 작성자로 사용 (나중에 로그인한 사람으로 교체 예정)
        Admin admin = adminRepository.findAll().get(0);

        InquiryComment comment = new InquiryComment();
        comment.setInquiry(inquiry);
        comment.setAdmin(admin);
        comment.setContent(content);

        inquiryCommentRepository.save(comment);

        return toCommentResponse(comment);
    }

    // 4. 상태 변경 (이력 기록 포함, 트랜잭션으로 묶임)
    public InquiryResponse updateStatus(Long id, InquiryStatus newStatus) {
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의입니다."));

        InquiryStatus oldStatus = inquiry.getStatus();

        inquiry.setStatus(newStatus);
        if (newStatus == InquiryStatus.DONE) {
            inquiry.setCompletedAt(java.time.LocalDate.now());
        }
        inquiryRepository.save(inquiry);

        // 임시로 첫 번째 관리자를 변경자로 사용 (나중에 로그인한 사람으로 교체 예정)
        Admin admin = adminRepository.findAll().get(0);

        InquiryStatusHistory history = new InquiryStatusHistory();
        history.setInquiry(inquiry);
        history.setBeforeStatus(oldStatus);
        history.setAfterStatus(newStatus);
        history.setChangedBy(admin);
        inquiryStatusHistoryRepository.save(history);

        return toResponse(inquiry);
    }

    // 5. 담당자 배정
    public InquiryResponse charge(Long id, Long adminId) {
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의입니다."));

        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));

        inquiry.setAssignee(admin);
        inquiryRepository.save(inquiry);

        return toResponse(inquiry);
    }

    // ===== 변환 도우미 메서드들 =====

    private InquiryResponse toResponse(Inquiry inquiry) {
        return new InquiryResponse(
                inquiry.getId(),
                inquiry.getUser().getName(),
                inquiry.getAssignee() != null ? inquiry.getAssignee().getName() : null,
                inquiry.getType(),
                inquiry.getTitle(),
                inquiry.getContent(),
                inquiry.getStatus(),
                inquiry.getCreatedAt(),
                inquiry.getCompletedAt()
        );
    }

    private InquiryCommentResponse toCommentResponse(InquiryComment comment) {
        return new InquiryCommentResponse(
                comment.getId(),
                comment.getAdmin().getName(),
                comment.getContent(),
                comment.getCreatedAt().atStartOfDay()
        );
    }

    private InquiryStatusHistoryResponse toHistoryResponse(InquiryStatusHistory history) {
        return new InquiryStatusHistoryResponse(
                history.getId(),
                history.getBeforeStatus(),
                history.getAfterStatus(),
                history.getChangedBy().getName(),
                history.getChangedAt()
        );
    }
}