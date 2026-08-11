package com.smconsole.inquiry;

import com.smconsole.admin.Admin;
import com.smconsole.admin.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import com.smconsole.auditlog.AuditAction;
import com.smconsole.auditlog.AuditLogService;
import org.springframework.transaction.annotation.Transactional;
import com.smconsole.auditlog.AuditTargetType;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;


import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InquiryService {
    private final InquiryCommentRepository inquiryCommentRepository;
    private final InquiryRepository inquiryRepository;
    private final InquiryStatusHistoryRepository inquiryStatusHistoryRepository;
    private final AdminRepository adminRepository;
    private final AuditLogService auditLogService;



    // 1. 목록조회 (검색+필터+페이징)
    public Page<InquiryResponse>getInquiry(
            String assigneeName, InquiryStatus status,
            InquiryType type, Pageable pageable) {
        Page<Inquiry> inquiries;

        if (assigneeName != null && !assigneeName.isEmpty()) {
            inquiries = inquiryRepository.findByAssigneeName(assigneeName, pageable);
        } else if (status != null) {
            inquiries = inquiryRepository.findByStatus(status, pageable);
        } else if (type != null) {
            inquiries = inquiryRepository.findByType(type, pageable);
        } else {
            inquiries = inquiryRepository.findAllFetch(pageable);
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
                inquiry.getAssignee() != null ? inquiry.getAssignee().getName():null,
                inquiry.getAssignee() != null ? inquiry.getAssignee().getId() : null,
                inquiry.getType(),
                inquiry.getTitle(),
                inquiry.getContent(),
                inquiry.getStatus(),
                inquiry.getCreatedAt(),
                inquiry.getCompletedAt(),
                inquiry.getVersion(),
                comments.stream().map(this::toCommentResponse).toList(),
                histories.stream().map(this::toHistoryResponse).toList()
        );
    }

    @Cacheable(value = "inquiryStats")
    public InquiryStatsResponse getStats(){
        long waiting = inquiryRepository.countByStatus(InquiryStatus.WAITING);
        long inProgress = inquiryRepository.countByStatus(InquiryStatus.IN_PROGRESS);
        long done = inquiryRepository.countByStatus(InquiryStatus.DONE);
        return new InquiryStatsResponse(waiting, inProgress, done);
    }

    // 3. 댓글 작성
    public InquiryCommentResponse createComment(Long inquiryId,String content) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의입니다."));

        // 임시로 첫 번째 관리자를 작성자로 사용 (나중에 로그인한 사람으로 교체 예정)
        Admin admin = getCurrentAdmin();

        InquiryComment comment = new InquiryComment();
        comment.setInquiry(inquiry);
        comment.setAdmin(admin);
        comment.setContent(content);

        inquiryCommentRepository.save(comment);

        return toCommentResponse(comment);
    }

    // 4. 상태 변경(이력 기록 포함, 트랜젹션으로 묶입)
    @CacheEvict(value = "inquiryStats", allEntries = true)
    public InquiryResponse updateStatus(Long id, InquiryStatus status, Long version){
        Inquiry inquiry = inquiryRepository.findByIdAndVersion(id, version)
                .orElseThrow(() -> new IllegalStateException("다른 관리자가 상태사항을 이미 수정했습니다. 새로고침 후 시도해 주세요."));

        inquiry.setVersion(version);

        InquiryStatus oldState = inquiry.getStatus();

        inquiry.setStatus(status);
        if (status == InquiryStatus.DONE){
            inquiry.setCompletedAt(java.time.LocalDate.now());
        }
        inquiryRepository.save(inquiry);

        Admin admin = getCurrentAdmin();

        InquiryStatusHistory history = new InquiryStatusHistory();
        history.setInquiry(inquiry);
        history.setBeforeStatus(oldState);
        history.setAfterStatus(status);
        history.setChangedBy(admin);
        inquiryStatusHistoryRepository.save(history);

        auditLogService.log(admin, AuditAction.UPDATE, AuditTargetType.INQUIRY, inquiry.getId(),
                "상태변경: " + oldState + " → " + status);

        return toResponse(inquiry);
    }

    // 5. 담당자 배정
    public InquiryResponse updatesAssign(Long id, Long adminId){
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의입니다."));

        if (adminId == null) {
            inquiry.setAssignee(null);   // 미배정으로 처리
        } else {
            Admin admin = adminRepository.findById(adminId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));
            inquiry.setAssignee(admin);
        }



        inquiryRepository.save(inquiry);
        return toResponse(inquiry);

    }

    private Admin getCurrentAdmin() {
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        return adminRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalStateException("로그인 정보를 찾을 수 없습니다."));
    }

    private  InquiryResponse toResponse(Inquiry inquiry){
        return new InquiryResponse(
                inquiry.getId(),
                inquiry.getUser().getName(),
                inquiry.getAssignee() != null ? inquiry.getAssignee().getName():null,
                inquiry.getAssignee() != null ? inquiry.getAssignee().getId() : null,
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