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
    // 라디오 방송을 실제로 내보내는 도구
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${incident.webhook.secret:}")
    private String webhookSecret;

    // 1. 목록조회
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

    // 2. 개별조회 (이력 포함)
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

    // 3. 통계
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

    // 4. 등록
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

        //방송 장비(실제 메시지를 보낼 수 있는)/ 변환해서 보내라/보낼 주소/보낼 내용물
        messagingTemplate.convertAndSend("/topic/incidents", toResponse(incident));

        auditLogService.log(incident.getReporter(), AuditAction.CREATE, AuditTargetType.INCIDENT, incident.getId(),
                "장애 등록: " + incident.getTitle());

        notifySlack("🚨 새 장애가 등록되었습니다\n제목: " + incident.getTitle() + "\n심각도: " + incident.getSeverity());

        return toResponse(incident);
    }


    // 5. 상태변경 (이력 기록 포함)
    @CacheEvict(value = "incidentStats", allEntries = true)
    public IncidentResponse updateStatus(Long id, IncidentStatus status, Long version) {
        if (version == null) {
            throw new IllegalArgumentException("버전 정보가 누락되었습니다.");
        }

        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 장애사항입니다."));

        // 클라이언트가 마지막으로 조회했던 버전과 지금 DB의 실제 버전을 비교한다.
        // (직접 확인한 내용: findById로 읽은 managed 엔티티에 incident.setVersion(version)으로
        //  값을 덮어써도 Hibernate는 그 값을 UPDATE의 WHERE 절에 쓰지 않는다 - flush 시점엔
        //  SELECT 때 캡처해둔 스냅샷 버전을 그대로 쓰기 때문에, 그 방식으로는 "클라이언트가 stale한
        //  버전을 보냈는지"를 전혀 검증하지 못한다. 그래서 이 비교는 명시적으로 남겨야 한다.)
        // 이 비교를 통과한 뒤에는 entity의 버전을 손대지 않으므로, save() 시점에 실제 동시 수정이
        // 있었다면(이 비교 통과 후 커밋 사이의 경쟁 구간) @Version이 자동으로
        // ObjectOptimisticLockingFailureException을 던진다 - 두 경우 모두 같은 예외/같은 409로 수렴한다.
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

        //방송 장비(실제 메시지를 보낼 수 있는)/ 변환해서 보내라/보낼 주소/보낼 내용물
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
            // 현재 FSM(RECEIVED → IN_PROGRESS → DONE, DONE은 종단 상태)에서는 DONE에서 다른 상태로
            // 되돌아가는 경로가 없어 이 분기가 지금 당장 실행되진 않는다. 다만 나중에 "재오픈" 전이가
            // 허용되거나 다른 경로로 상태가 바뀌더라도 resolvedAt이 stale하게 남지 않도록 방어적으로 둔다.
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
