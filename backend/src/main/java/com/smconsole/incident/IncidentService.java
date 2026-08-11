package com.smconsole.incident;

import com.smconsole.admin.Admin;
import com.smconsole.admin.AdminRepository;
import com.smconsole.notification.SlackNotificationService;
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


import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final IncidentStatusHistoryRepository incidentStatusHistoryRepository;
    private final AdminRepository adminRepository;
    private final AuditLogService auditLogService;
    private final SlackNotificationService slackNotificationService;

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

        auditLogService.log(incident.getReporter(), AuditAction.CREATE, AuditTargetType.INCIDENT, incident.getId(),
                "장애 등록: " + incident.getTitle());

        try {
            slackNotificationService.notification(
                    "🚨 새 장애가 등록되었습니다\n제목: " + incident.getTitle() + "\n심각도: " + incident.getSeverity()
            );
        } catch (Exception e) {
            System.out.println("Slack 알림 전송 실패: " + e.getMessage());
        }



        return toResponse(incident);
    }


    // 5. 상태변경 (이력 기록 포함)
    @CacheEvict(value = "incidentStats", allEntries = true)
    public IncidentResponse updateStatus(Long id, IncidentStatus status, Long version) {
        Incident incident = incidentRepository.findByIdAndVersion(id, version)
                .orElseThrow(() -> new IllegalStateException("다른 관리자가 상태사항을 이미 수정했습니다. 새로고침 후 시도해 주세요."));

        incident.setVersion(version);


        IncidentStatus oldStatus = incident.getStatus();

        incident.setStatus(status);
        if (status == IncidentStatus.DONE) {
            incident.setResolvedAt(LocalDateTime.now());
        }
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

    public IncidentResponse createWebhook(WebhookIncidentRequest request){
        Incident incident = new Incident();

        incident.setTitle(request.title());
        incident.setContent(request.content());
        incident.setSeverity(request.severity());
        incident.setStatus(IncidentStatus.RECEIVED);
        incident.setOccurredAt(LocalDateTime.now());

        incidentRepository.save(incident);

        try {
            slackNotificationService.notification(
                    "🤖 자동 감지된 장애가 등록되었습니다\n제목: " + incident.getTitle() + "\n심각도: " + incident.getSeverity()
            );
        } catch (Exception e) {
            System.out.println("Slack 알림 전송 실패: " + e.getMessage());
        }

        return toResponse(incident);
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