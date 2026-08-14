package com.smconsole.incident;

import com.smconsole.admin.entity.Admin;
import com.smconsole.admin.repository.AdminRepository;
import com.smconsole.auditlog.AuditLogService;
import com.smconsole.incident.entity.Incident;
import com.smconsole.incident.enums.IncidentSeverity;
import com.smconsole.incident.enums.IncidentStatus;
import com.smconsole.incident.repository.IncidentRepository;
import com.smconsole.incident.dto.IncidentResponse;
import com.smconsole.incident.repository.IncidentStatusHistoryRepository;
import com.smconsole.incident.dto.WebhookIncidentRequest;
import com.smconsole.incident.service.IncidentService;
import com.smconsole.notification.SlackNotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * IncidentService의 낙관적 락 정리 / 상태 전이 검증 / 웹훅 시크릿 검증을 DB 없이 검증하는 순수 단위 테스트.
 * (이 리포지토리엔 incident 도메인에 대한 기존 테스트가 없어서, 이번에 고친 로직 전용으로 새로 추가함)
 */
@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {

    @Mock
    private IncidentRepository incidentRepository;
    @Mock
    private IncidentStatusHistoryRepository incidentStatusHistoryRepository;
    @Mock
    private AdminRepository adminRepository;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private SlackNotificationService slackNotificationService;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private IncidentService incidentService;

    @BeforeEach
    void setUp() {
        incidentService = new IncidentService(
                incidentRepository,
                incidentStatusHistoryRepository,
                adminRepository,
                auditLogService,
                slackNotificationService,
                messagingTemplate
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Incident incidentWithStatus(IncidentStatus status, Long version) {
        Incident incident = new Incident();
        incident.setId(1L);
        incident.setTitle("t");
        incident.setContent("c");
        incident.setSeverity(IncidentSeverity.HIGH);
        incident.setStatus(status);
        incident.setVersion(version);
        return incident;
    }

    private void loginAs(String loginId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginId, null)
        );
        Admin admin = new Admin();
        admin.setId(1L);
        admin.setLoginId(loginId);
        admin.setName("관리자");
        lenient().when(adminRepository.findByLoginId(loginId)).thenReturn(Optional.of(admin));
    }

    // ---------- 1. 낙관적 락 ----------

    @Test
    void updateStatus_버전이_null이면_400성_예외로_막는다() {
        assertThatThrownBy(() -> incidentService.updateStatus(1L, IncidentStatus.IN_PROGRESS, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("버전 정보가 누락되었습니다.");

        // 버전이 없으면 조회조차 하지 않고 막아야 한다 (낙관적 락 충돌과 구분되는 별도 400 케이스)
        verify(incidentRepository, never()).findById(any());
    }

    @Test
    void updateStatus_존재하지_않으면_400() {
        when(incidentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incidentService.updateStatus(1L, IncidentStatus.IN_PROGRESS, 0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 장애사항입니다.");
    }

    @Test
    void updateStatus_클라이언트가_보낸_버전이_실제_버전과_다르면_충돌로_막는다() {
        // DB엔 이미 버전 5까지 진행된 상태인데, 클라이언트는 버전 2를 들고 요청한 상황(= 다른 관리자가
        // 그 사이 먼저 수정함). findById는 항상 "현재" 상태(버전 5)를 읽어오므로, 이 불일치를 잡으려면
        // 명시적으로 비교해야 한다 - entity.setVersion(2)로 덮어써도 Hibernate가 그 값을 UPDATE의
        // WHERE 절에 반영해주지 않기 때문에(직접 확인함), 그 방식으로는 이 케이스를 절대 못 잡는다.
        Incident incident = incidentWithStatus(IncidentStatus.RECEIVED, 5L);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        assertThatThrownBy(() -> incidentService.updateStatus(1L, IncidentStatus.IN_PROGRESS, 2L))
                .isInstanceOf(org.springframework.orm.ObjectOptimisticLockingFailureException.class);

        verify(incidentRepository, never()).save(any());
    }

    @Test
    void updateStatus_클라이언트가_보낸_버전이_실제_버전과_같으면_통과한다() {
        Incident incident = incidentWithStatus(IncidentStatus.RECEIVED, 5L);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        loginAs("admin1");

        incidentService.updateStatus(1L, IncidentStatus.IN_PROGRESS, 5L);

        verify(incidentRepository).save(incident);
    }

    // ---------- 2. 상태 전이 검증 ----------

    @Test
    void updateStatus_RECEIVED에서_IN_PROGRESS로는_허용된다() {
        Incident incident = incidentWithStatus(IncidentStatus.RECEIVED, 0L);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        loginAs("admin1");

        IncidentResponse response = incidentService.updateStatus(1L, IncidentStatus.IN_PROGRESS, 0L);

        assertThat(response.status()).isEqualTo(IncidentStatus.IN_PROGRESS);
    }

    @Test
    void updateStatus_RECEIVED에서_DONE으로_건너뛰기는_막는다() {
        Incident incident = incidentWithStatus(IncidentStatus.RECEIVED, 0L);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        assertThatThrownBy(() -> incidentService.updateStatus(1L, IncidentStatus.DONE, 0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("허용되지 않는 상태 전이");

        verify(incidentRepository, never()).save(any());
    }

    @Test
    void updateStatus_DONE에서_RECEIVED로_되돌리기는_막는다() {
        Incident incident = incidentWithStatus(IncidentStatus.DONE, 0L);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        assertThatThrownBy(() -> incidentService.updateStatus(1L, IncidentStatus.RECEIVED, 0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("허용되지 않는 상태 전이");
    }

    @Test
    void updateStatus_같은_상태로의_중복_변경은_막는다() {
        Incident incident = incidentWithStatus(IncidentStatus.IN_PROGRESS, 0L);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        assertThatThrownBy(() -> incidentService.updateStatus(1L, IncidentStatus.IN_PROGRESS, 0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미");
    }

    @Test
    void updateStatus_DONE으로_바뀌면_resolvedAt이_세팅된다() {
        Incident incident = incidentWithStatus(IncidentStatus.IN_PROGRESS, 0L);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        loginAs("admin1");

        incidentService.updateStatus(1L, IncidentStatus.DONE, 0L);

        assertThat(incident.getResolvedAt()).isNotNull();
    }

    // ---------- 3. 웹훅 시크릿 검증 ----------

    private WebhookIncidentRequest webhookRequest() {
        return new WebhookIncidentRequest(IncidentSeverity.CRITICAL, "제목", "내용");
    }

    @Test
    void createWebhook_시크릿이_서버에_설정되지_않으면_모두_막는다() {
        ReflectionTestUtils.setField(incidentService, "webhookSecret", "");

        assertThatThrownBy(() -> incidentService.createWebhook(webhookRequest(), "아무값"))
                .isInstanceOf(BadCredentialsException.class);

        verify(incidentRepository, never()).save(any());
    }

    @Test
    void createWebhook_시크릿이_틀리면_막는다() {
        ReflectionTestUtils.setField(incidentService, "webhookSecret", "real-secret");

        assertThatThrownBy(() -> incidentService.createWebhook(webhookRequest(), "wrong-secret"))
                .isInstanceOf(BadCredentialsException.class);

        verify(incidentRepository, never()).save(any());
    }

    @Test
    void createWebhook_헤더가_아예_없으면_막는다() {
        ReflectionTestUtils.setField(incidentService, "webhookSecret", "real-secret");

        assertThatThrownBy(() -> incidentService.createWebhook(webhookRequest(), null))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void createWebhook_시크릿이_맞으면_통과하고_감사로그도_남긴다() {
        ReflectionTestUtils.setField(incidentService, "webhookSecret", "real-secret");
        when(incidentRepository.save(any())).thenAnswer(inv -> {
            Incident i = inv.getArgument(0);
            i.setId(42L);
            return i;
        });

        IncidentResponse response = incidentService.createWebhook(webhookRequest(), "real-secret");

        assertThat(response.title()).isEqualTo("제목");
        verify(auditLogService).log(
                org.mockito.ArgumentMatchers.isNull(),
                any(), any(), any(), any()
        );
        verify(messagingTemplate).convertAndSend("/topic/incidents", response);
    }
}
