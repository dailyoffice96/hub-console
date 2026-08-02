package com.smconsole.config;

import com.smconsole.admin.Admin;
import com.smconsole.admin.AdminRepository;
import com.smconsole.admin.AdminRole;
import com.smconsole.auditlog.AuditAction;
import com.smconsole.auditlog.AuditLog;
import com.smconsole.auditlog.AuditLogRepository;
import com.smconsole.auditlog.AuditTargetType;
import com.smconsole.incident.Incident;
import com.smconsole.incident.IncidentRepository;
import com.smconsole.incident.IncidentSeverity;
import com.smconsole.incident.IncidentStatus;
import com.smconsole.inquiry.*;
import com.smconsole.user.User;
import com.smconsole.user.UserRepository;
import com.smconsole.user.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final InquiryRepository inquiryRepository;
    private final IncidentRepository incidentRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (adminRepository.findByLoginId("super01").isEmpty()) {
            Admin admin1 = new Admin();
            admin1.setLoginId("super01");
            admin1.setPasswordHash(passwordEncoder.encode("1234"));
            admin1.setName("대표");
            admin1.setRole(AdminRole.SUPER_ADMIN);
            adminRepository.save(admin1);
        }

        if (adminRepository.findByLoginId("admin01").isEmpty()) {
            Admin admin2 = new Admin();
            admin2.setLoginId("admin01");
            admin2.setPasswordHash(passwordEncoder.encode("1234"));
            admin2.setName("팀장");
            admin2.setRole(AdminRole.ADMIN);
            adminRepository.save(admin2);
        }

        if (adminRepository.findByLoginId("staff01").isEmpty()) {
            Admin admin3 = new Admin();
            admin3.setLoginId("staff01");
            admin3.setPasswordHash(passwordEncoder.encode("1234"));
            admin3.setName("직원");
            admin3.setRole(AdminRole.STAFF);
            adminRepository.save(admin3);
        }

        if (userRepository.count() == 0) {
            String[] names = {"장원영", "김철수", "이영희", "박민수", "최지훈", "장원영", "박영희", "박민수", "홍길동", "홍홍", "홍철"};
            String[] phones = {"010-1111-2222", "010-2222-3333", "010-3333-4444", "010-4444-5555", "010-5555-6666", "010-1111-2322", "010-2222-3313", "010-3333-4144", "010-4444-5525", "010-5555-3666", "010-6666-7477"};

            for (int i = 0; i < names.length; i++) {
                User user = new User();
                user.setLoginId("user" + (i + 1));
                user.setName(names[i]);
                user.setPhone(phones[i]);
                user.setEmail("user" + (i + 1) + "@test.com");
                user.setStatus(UserStatus.ACTIVE);
                userRepository.save(user);
            }
        }

        if (inquiryRepository.count() == 0) {
            List<User> allUsers = userRepository.findAll();
            Admin assignee = adminRepository.findByLoginId("admin01").orElse(null);

            if (!allUsers.isEmpty()) {
                String[] titles = {"로그인이 안 돼요", "환불 요청합니다", "화면이 이상해요", "탈퇴하고 싶어요", "결제 오류 문의"};
                InquiryType[] types = {InquiryType.ACCOUNT, InquiryType.PAYMENT, InquiryType.TECHNICAL, InquiryType.SERVICE, InquiryType.ETC};
                InquiryStatus[] statuses = {InquiryStatus.WAITING, InquiryStatus.IN_PROGRESS, InquiryStatus.DONE, InquiryStatus.WAITING, InquiryStatus.IN_PROGRESS};

                for (int i = 0; i < titles.length; i++) {
                    Inquiry inquiry = new Inquiry();
                    inquiry.setUser(allUsers.get(i % allUsers.size()));   // 반드시 존재하는 회원을 순환하며 배정
                    inquiry.setAssignee(i % 2 == 0 ? assignee : null);
                    inquiry.setType(types[i]);
                    inquiry.setTitle(titles[i]);
                    inquiry.setContent(titles[i] + "에 대한 상세 문의 내용입니다.");
                    inquiry.setStatus(statuses[i]);
                    inquiryRepository.save(inquiry);
                }
            }
        }

        // 장애 목록 시드 데이터
        if (incidentRepository.count() == 0) {
            Admin reporter1 = adminRepository.findByLoginId("admin01").orElse(null);
            Admin reporter2 = adminRepository.findByLoginId("staff01").orElse(null);

            if (reporter1 != null && reporter2 != null) {
                String[] titles = {"결제 서버 응답 지연", "로그인 세션 만료 오류", "회원 조회 API 500 에러", "이미지 업로드 실패", "배치 작업 미실행"};
                String[] contents = {
                        "결제 API 응답 시간이 평소보다 5배 이상 느려짐. 트래픽 급증 추정.",
                        "일부 사용자 로그인 후 세션이 비정상적으로 빨리 만료되는 현상 발생.",
                        "회원 상세조회 API 호출 시 간헐적으로 500 에러 발생, 로그 확인 필요.",
                        "프로필 이미지 업로드 시 파일 크기 무관하게 실패하는 현상.",
                        "야간 배치(정산) 작업이 스케줄대로 실행되지 않음."
                };
                IncidentSeverity[] severities = {IncidentSeverity.CRITICAL, IncidentSeverity.HIGH, IncidentSeverity.MEDIUM, IncidentSeverity.LOW, IncidentSeverity.HIGH};
                IncidentStatus[] statuses = {IncidentStatus.RECEIVED, IncidentStatus.IN_PROGRESS, IncidentStatus.DONE, IncidentStatus.RECEIVED, IncidentStatus.IN_PROGRESS};
                Admin[] reporters = {reporter1, reporter2, reporter1, reporter2, reporter1};

                for (int i = 0; i < titles.length; i++) {
                    Incident incident = new Incident();
                    incident.setTitle(titles[i]);
                    incident.setContent(contents[i]);
                    incident.setSeverity(severities[i]);
                    incident.setStatus(statuses[i]);
                    incident.setReporter(reporters[i]);
                    incident.setOccurredAt(LocalDateTime.now().minusHours(i + 1));
                    incident.setSladueAt(LocalDateTime.now().plusHours(24 - i));
                    if (statuses[i] == IncidentStatus.DONE) {
                        incident.setResolvedAt(LocalDateTime.now().minusMinutes(30));
                    }
                    incidentRepository.save(incident);
                }
            }
        }

        // 감사로그 시드 데이터
        if (auditLogRepository.count() == 0) {
            Admin superAdmin = adminRepository.findByLoginId("super01").orElse(null);
            Admin admin = adminRepository.findByLoginId("admin01").orElse(null);
            Admin staff = adminRepository.findByLoginId("staff01").orElse(null);

            if (superAdmin != null && admin != null && staff != null) {
                AuditLog log1 = new AuditLog();
                log1.setAdmin(admin);
                log1.setAction(AuditAction.CREATE);
                log1.setTargetType(AuditTargetType.INCIDENT);
                log1.setTargetId(1L);
                log1.setDetail("장애 등록: 결제 서버 응답 지연");
                auditLogRepository.save(log1);

                AuditLog log2 = new AuditLog();
                log2.setAdmin(staff);
                log2.setAction(AuditAction.UPDATE);
                log2.setTargetType(AuditTargetType.INCIDENT);
                log2.setTargetId(2L);
                log2.setDetail("상태변경: RECEIVED → IN_PROGRESS");
                auditLogRepository.save(log2);

                AuditLog log3 = new AuditLog();
                log3.setAdmin(admin);
                log3.setAction(AuditAction.UPDATE);
                log3.setTargetType(AuditTargetType.INCIDENT);
                log3.setTargetId(3L);
                log3.setDetail("상태변경: IN_PROGRESS → RESOLVED");
                auditLogRepository.save(log3);

                AuditLog log4 = new AuditLog();
                log4.setAdmin(admin);
                log4.setAction(AuditAction.UPDATE);
                log4.setTargetType(AuditTargetType.INQUIRY);
                log4.setTargetId(1L);
                log4.setDetail("상태변경: WAITING → IN_PROGRESS");
                auditLogRepository.save(log4);

                AuditLog log5 = new AuditLog();
                log5.setAdmin(superAdmin);
                log5.setAction(AuditAction.DELETE);
                log5.setTargetType(AuditTargetType.ADMIN);
                log5.setTargetId(99L);
                log5.setDetail("관리자 계정 삭제: 테스트계정(test99)");
                auditLogRepository.save(log5);
            }
        }
    }
}