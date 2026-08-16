package com.smconsole.incident.service;

import com.smconsole.admin.entity.Admin;
import com.smconsole.admin.repository.AdminRepository;
import com.smconsole.incident.dto.*;
import com.smconsole.incident.entity.Incident;
import com.smconsole.incident.entity.IncidentStatusHistory;
import com.smconsole.incident.enums.IncidentSeverity;
import com.smconsole.incident.enums.IncidentStatus;
import com.smconsole.incident.repository.*;
import com.smconsole.notification.SlackNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import com.smconsole.auditlog.AuditAction;
import com.smconsole.auditlog.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.smconsole.auditlog.AuditTargetType;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class IncidentService {

    // RECEIVED → IN_PROGRESS → DONE 순서만 허용한다. 건너뛰기/되돌리기/동일 상태로의 변경은 모두 막는다.
    // DONE은 이 맵에 키가 없으므로(= 다음 허용 상태가 없으므로) 종단 상태가 된다.
    private static final Map<IncidentStatus, IncidentStatus> ALLOWED_NEXT_STATUS = Map.of(
            IncidentStatus.RECEIVED, IncidentStatus.IN_PROGRESS,
            IncidentStatus.IN_PROGRESS, IncidentStatus.DONE
    );

    private static final String WEBHOOK_AUTH_FAILED_MESSAGE = "웹훅 인증에 실패했습니다.";

    private final IncidentRepository incidentRepository;
    private final IncidentStatusHistoryRepository incidentStatusHistoryRepository;
    private final AdminRepository adminRepository;
    private final AuditLogService auditLogService;
    private final SlackNotificationService slackNotificationService;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${incident.webhook.secret:}")
    private String webhookSecret;

    public Page<IncidentResponse> getIncident(
            String reporterName, IncidentStatus status, IncidentSeverity severity, Pageable pageable) {
        Page<Incident> incidents;

        if (reporterName != null && !reporterName.isEmpty()) {
            incidents = incidentRepository.findByReporter_NameContaining(reporterName, pageable);
        } else if (status != null) {
            incidents = incidentRepository.findByStatus(status, pageable);
        } else if (severity != null) {
            incidents = incidentRepository.findBySeverity(severity, pageable);
        } else {
            incidents = incidentRepository.findAllFetch(pageable);
        }

        return incidents.map(this::toResponse);
    }

    public IncidentDetailResponse getDetail(Long id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 장애사항입니다."));

        List<IncidentStatusHistory> histories = incidentStatusHistoryRepository.findByIncidentId(id);

        return new IncidentDetailResponse(
                incident.getId(),
                incident.getTitle(),
                incident.getContent(),
                incident.getSeverity(),
                incident.getStatus(),
                incident.getReporter() != null ? incident.getReporter().getName() : null,
                incident.getOccurredAt(),
                incident.getSladueAt(),
                incident.getResolvedAt(),
                incident.getCreatedAt(),
                incident.getVersion(),
                histories.stream().map(this::toHistoryResponse).toList()
        );
    }

    @Cacheable(value = "incidentStats")
    public IncidentStatsResponse getStats() {
        long received = incidentRepository.countByStatus(IncidentStatus.RECEIVED);
        long inProgress = incidentRepository.countByStatus(IncidentStatus.IN_PROGRESS);
        long done = incidentRepository.countByStatus(IncidentStatus.DONE);
        return new IncidentStatsResponse(received, inProgress, done);
    }

    public IncidentSeverityResponse getSeverityStats() {
        long critical = incidentRepository.countBySeverity(IncidentSeverity.CRITICAL);
        long high = incidentRepository.countBySeverity(IncidentSeverity.HIGH);
        long medium = incidentRepository.countBySeverity(IncidentSeverity.MEDIUM);
        long low = incidentRepository.countBySeverity(IncidentSeverity.LOW);
        return new IncidentSeverityResponse(critical, high, medium, low);
    }

    @CacheEvict(value = "incidentStats", allEntries = true)
    public IncidentResponse createIncident(IncidentCreateRequest request) {
        Incident incident = new Incident();
        incident.setTitle(request.title());
        incident.setContent(request.content());
        incident.setSeverity(request.severity());
        incident.setOccurredAt(request.occurredAt() != null ? request.occurredAt() : LocalDateTime.now());
        incident.setSladueAt(request.slaDueAt());
        incident.setStatus(IncidentStatus.RECEIVED);
        incident.setReporter(getCurrentAdmin());

        incidentRepository.save(incident);

        messagingTemplate.convertAndSend("/topic/incidents", toResponse(incident));

        auditLogService.log(incident.getReporter(), AuditAction.CREATE, AuditTargetType.INCIDENT, incident.getId(),
                "장애 등록: " + incident.getTitle());

        notifySlack("🚨 새 장애가 등록되었습니다\n제목: " + incident.getTitle() + "\n심각도: " + incident.getSeverity());

        return toResponse(incident);
    }


    @CacheEvict(value = "incidentStats", allEntries = true)
    public IncidentResponse updateStatus(Long id, IncidentStatus status, Long version) {
        if (version == null) {
            throw new IllegalArgumentException("버전 정보가 누락되었습니다.");
        }

        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 장애사항입니다."));

        // 클라이언트가 마지막으로 본 버전과 지금 DB 버전을 비교해서 오래된 데이터로 수정하는 걸 막는다
        // (entity에 버전 값을 그냥 덮어써도 JPA가 그걸로 WHERE절을 안 만들어서 직접 비교해야 함).
        // 이 비교 통과 후 실제로 동시 수정이 생기면, 저장 시점에 @Version이 알아서 예외를 던져준다.
        if (!incident.getVersion().equals(version)) {
            throw new ObjectOptimisticLockingFailureException(Incident.class, id);
        }

        IncidentStatus oldStatus = incident.getStatus();
        validateTransition(oldStatus, status);
        applyStatus(incident, status);

        incidentRepository.save(incident);

        Admin admin = getCurrentAdmin();

        IncidentStatusHistory history = new IncidentStatusHistory();
        history.setIncident(incident);
        history.setBeforeStatus(oldStatus);
        history.setAfterStatus(status);
        history.setChangedBy(admin);
        incidentStatusHistoryRepository.save(history);

        auditLogService.log(admin, AuditAction.UPDATE, AuditTargetType.INCIDENT, incident.getId(),
                "상태변경: " + oldStatus + " → " + status);

        return toResponse(incident);
    }

    @CacheEvict(value = "incidentStats", allEntries = true)
    public IncidentResponse createWebhook(WebhookIncidentRequest request, String providedSecret){
        validateWebhookSecret(providedSecret);

        Incident incident = new Incident();

        incident.setTitle(request.title());
        incident.setContent(request.content());
        incident.setSeverity(request.severity());
        incident.setStatus(IncidentStatus.RECEIVED);
        incident.setOccurredAt(LocalDateTime.now());

        incidentRepository.save(incident);

        messagingTemplate.convertAndSend("/topic/incidents", toResponse(incident));

        auditLogService.log(null, AuditAction.CREATE, AuditTargetType.INCIDENT, incident.getId(),
                "웹훅으로 장애 자동 등록: " + incident.getTitle());

        notifySlack("🤖 자동 감지된 장애가 등록되었습니다\n제목: " + incident.getTitle() + "\n심각도: " + incident.getSeverity());

        return toResponse(incident);
    }

    private void validateWebhookSecret(String providedSecret) {
        boolean secretConfigured = webhookSecret != null && !webhookSecret.isBlank();
        boolean matches = secretConfigured
                && providedSecret != null
                && MessageDigest.isEqual(
                        providedSecret.getBytes(StandardCharsets.UTF_8),
                        webhookSecret.getBytes(StandardCharsets.UTF_8));

        if (!matches) {
            if (!secretConfigured) {
                log.warn("INCIDENT_WEBHOOK_SECRET이 설정되지 않아 /api/incidents/webhook 호출을 모두 차단합니다.");
            }
            throw new BadCredentialsException(WEBHOOK_AUTH_FAILED_MESSAGE);
        }
    }

    private void validateTransition(IncidentStatus from, IncidentStatus to) {
        if (to == null) {
            throw new IllegalArgumentException("상태 값이 누락되었습니다.");
        }
        if (from == to) {
            throw new IllegalArgumentException("이미 '" + from + "' 상태입니다.");
        }
        if (ALLOWED_NEXT_STATUS.get(from) != to) {
            throw new IllegalArgumentException(
                    "허용되지 않는 상태 전이입니다: " + from + " → " + to
                            + " (허용된 순서: RECEIVED → IN_PROGRESS → DONE)"
            );
        }
    }

    private void applyStatus(Incident incident, IncidentStatus newStatus) {
        incident.setStatus(newStatus);
        if (newStatus == IncidentStatus.DONE) {
            incident.setResolvedAt(LocalDateTime.now());
        } else if (incident.getResolvedAt() != null) {
            // 지금은 DONE에서 되돌아가는 전이가 없어서 당장 타지는 않지만, 나중에 재오픈이
            // 허용되더라도 resolvedAt이 남아있지 않게 방어적으로 초기화해둔다.
            incident.setResolvedAt(null);
        }
    }

    private void notifySlack(String message) {
        try {
            slackNotificationService.notification(message);
        } catch (Exception e) {
            log.warn("Slack 알림 전송 실패: {}", e.getMessage(), e);
        }
    }

    private Admin getCurrentAdmin() {
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        return adminRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalStateException("로그인 정보를 찾을 수 없습니다."));
    }

    private IncidentResponse toResponse(Incident incident) {
        return new IncidentResponse(
                incident.getId(),
                incident.getTitle(),
                incident.getSeverity(),
                incident.getStatus(),
                incident.getReporter() != null ? incident.getReporter().getName() : null,
                incident.getOccurredAt(),
                incident.getSladueAt()
        );
    }

    private IncidentStatusHistoryResponse toHistoryResponse(IncidentStatusHistory history) {
        return new IncidentStatusHistoryResponse(
                history.getId(),
                history.getBeforeStatus(),
                history.getAfterStatus(),
                history.getChangedBy() != null ? history.getChangedBy().getName() : null,
                history.getChangedAt()
        );
    }
}
