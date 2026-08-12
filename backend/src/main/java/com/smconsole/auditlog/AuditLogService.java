package com.smconsole.auditlog;

import com.smconsole.admin.Admin;
import com.smconsole.ai.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final OpenAiService openAiService;

    private static final int SUSPICIOUS_LOG_HOURS = 1;

    public void log(Admin admin, AuditAction action, AuditTargetType targetType, Long targetId, String detail) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAdmin(admin);
        auditLog.setAction(action);
        auditLog.setTargetType(targetType);
        auditLog.setTargetId(targetId);
        auditLog.setDetail(detail);
        auditLogRepository.save(auditLog);
    }

    public Page<AuditLogResponse> getLog(
            String adminName, AuditTargetType targetType, AuditAction action, Pageable pageable
    ) {
        Page<AuditLog> auditLog;

        if (adminName != null && !adminName.isEmpty()) {
            auditLog = auditLogRepository.findByAdmin_NameContaining(adminName, pageable);
        } else if (targetType != null) {
            auditLog = auditLogRepository.findByTargetType(targetType, pageable);
        } else if (action != null) {
            auditLog = auditLogRepository.findByAction(action, pageable);
        } else {
            auditLog = auditLogRepository.findAllFetch(pageable);
        }

        return auditLog.map(this::toResponse);
    }


    public List<AuditLog> findSuspiciousLogs(){
        LocalDateTime timeDate = LocalDateTime.now().minusHours(SUSPICIOUS_LOG_HOURS);
        return auditLogRepository.findByCreatedAtAfter(timeDate);
    }

    public String analyze(){
        List<AuditLog> logs = findSuspiciousLogs();

        if(logs.isEmpty()){
            return "최근 1시간 내 특이사항이 없습니다.";
        }

        StringBuilder logText = new StringBuilder();
        for (AuditLog log:logs){
            logText.append(log.getAdmin() != null ? log.getAdmin().getName() : "알 수 없음")
                    .append(" - ")
                    .append(log.getAction())
                    .append(" - ")
                    .append(log.getDetail())
                    .append(" (")
                    .append(log.getCreatedAt())
                    .append(")\n");
        }

        String prompt = "다음은 관리 시스템의 최근 1시간 감사로그입니다. " +
                "비정상적이거나 의심스러운 패턴(예: 짧은 시간 내 대량 삭제, 새벽 시간대 활동 등)이 있다면 " +
                "한국어로 간단히 설명해주세요. 없다면 '특이사항 없음'이라고 답해주세요.\n\n" + logText;

        try {
            return openAiService.analyze(prompt);
        } catch (Exception e){
            throw new IllegalStateException("AI 분석 중 오류가 발생했습니다: " + e.getMessage());
        }
    }


    private AuditLogResponse toResponse(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getAdmin() != null ? auditLog.getAdmin().getName() : null,
                auditLog.getAction(),
                auditLog.getTargetId(),
                auditLog.getTargetType(),
                auditLog.getDetail(),
                auditLog.getCreatedAt()
        );
    }
}