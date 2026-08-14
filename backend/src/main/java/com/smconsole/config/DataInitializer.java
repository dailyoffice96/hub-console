package com.smconsole.config;

import com.smconsole.admin.entity.Admin;
import com.smconsole.admin.repository.AdminRepository;
import com.smconsole.admin.enums.AdminRole;
import com.smconsole.auditlog.AuditAction;
import com.smconsole.auditlog.AuditLog;
import com.smconsole.auditlog.AuditLogRepository;
import com.smconsole.auditlog.AuditTargetType;
import com.smconsole.incident.entity.Incident;
import com.smconsole.incident.repository.IncidentRepository;
import com.smconsole.incident.enums.IncidentSeverity;
import com.smconsole.incident.enums.IncidentStatus;
import com.smconsole.inquiry.entity.Inquiry;
import com.smconsole.inquiry.enums.InquiryStatus;
import com.smconsole.inquiry.enums.InquiryType;
import com.smconsole.inquiry.repository.InquiryRepository;
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

        // 1. 관리자 계정 시드 데이터
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

        // 2. 사용자 시드 데이터 (오늘 가입한 데이터 포함)
        if (userRepository.count() == 0) {
            String[] names = {"장원영", "김철수", "이영희", "박민수", "최지훈"};
            String[] phones = {"010-1111-2222", "010-2222-3333", "010-3333-4444", "010-4444-5555", "010-5555-6666"};

            for (int i = 0; i < names.length; i++) {
                User user = new User();
                user.setLoginId("user" + (i + 1));
                user.setName(names[i]);
                user.setPhone(phones[i]);
                user.setEmail("user" + (i + 1) + "@test.com");
                user.setStatus(UserStatus.ACTIVE);
                // 만약 User 엔티티에 가입일 필드(createdAt)가 있다면 오늘 날짜로 설정
                // user.setCreatedAt(LocalDateTime.now().minusHours(i));
                userRepository.save(user);
            }
        }

        // 3. 오늘 날짜 기준 추가 사용자 (통계 집계 테스트용)
        if (userRepository.findByLoginId("today_user1").isEmpty()) {
            User todayUser1 = new User();
            todayUser1.setLoginId("today_user1");
            todayUser1.setName("신규유저1");
            todayUser1.setPhone("010-9999-1111");
            todayUser1.setEmail("today1@test.com");
            todayUser1.setStatus(UserStatus.ACTIVE);
            userRepository.save(todayUser1);

            User todayUser2 = new User();
            todayUser2.setLoginId("today_user2");
            todayUser2.setName("신규유저2");
            todayUser2.setPhone("010-9999-2222");
            todayUser2.setEmail("today2@test.com");
            todayUser2.setStatus(UserStatus.ACTIVE);
            userRepository.save(todayUser2);
        }

        // 4. 문의사항 시드 데이터
        if (inquiryRepository.count() == 0) {
            List<User> allUsers = userRepository.findAll();
            Admin assignee = adminRepository.findByLoginId("admin01").orElse(null);

            if (!allUsers.isEmpty()) {
                String[] titles = {"로그인이 안 돼요", "환불 요청합니다", "화면이 이상해요", "오늘 가입했는데 오류나요"};
                InquiryType[] types = {InquiryType.ACCOUNT, InquiryType.PAYMENT, InquiryType.TECHNICAL, InquiryType.SERVICE};
                InquiryStatus[] statuses = {InquiryStatus.WAITING, InquiryStatus.IN_PROGRESS, InquiryStatus.DONE, InquiryStatus.WAITING};

                for (int i = 0; i < titles.length; i++) {
                    Inquiry inquiry = new Inquiry();
                    inquiry.setUser(allUsers.get(i % allUsers.size()));
                    inquiry.setAssignee(i % 2 == 0 ? assignee : null);
                    inquiry.setType(types[i]);
                    inquiry.setTitle(titles[i]);
                    inquiry.setContent(titles[i] + "에 대한 상세 문의 내용입니다.");
                    inquiry.setStatus(statuses[i]);
                    inquiryRepository.save(inquiry);
                }
            }
        }

        // 5. 장애 목록 시드 데이터 (오늘 발생한 장애 포함)
        if (incidentRepository.count() == 0) {
            Admin reporter1 = adminRepository.findByLoginId("admin01").orElse(null);
            Admin reporter2 = adminRepository.findByLoginId("staff01").orElse(null);

            if (reporter1 != null && reporter2 != null) {
                String[] titles = {"[긴급] 오늘 오전 결제 연동 오류", "로그인 세션 만료 이슈", "회원 조회 API 500 에러"};
                String[] contents = {
                        "오늘 오전부터 PG사 연동 과정에서 타임아웃 발생.",
                        "일부 사용자 로그인 후 세션이 비정상적으로 만료됨.",
                        "회원 상세조회 API 호출 시 간헐적으로 500 에러 발생."
                };
                IncidentSeverity[] severities = {IncidentSeverity.CRITICAL, IncidentSeverity.HIGH, IncidentSeverity.MEDIUM};
                IncidentStatus[] statuses = {IncidentStatus.RECEIVED, IncidentStatus.IN_PROGRESS, IncidentStatus.DONE};
                Admin[] reporters = {reporter1, reporter2, reporter1};

                for (int i = 0; i < titles.length; i++) {
                    Incident incident = new Incident();
                    incident.setTitle(titles[i]);
                    incident.setContent(contents[i]);
                    incident.setSeverity(severities[i]);
                    incident.setStatus(statuses[i]);
                    incident.setReporter(reporters[i]);
                    // 오늘 발생한 시간으로 설정 (예: 2시간 전, 1시간 전 등)
                    incident.setOccurredAt(LocalDateTime.now().minusHours(i + 1));
                    incident.setSladueAt(LocalDateTime.now().plusHours(24));
                    if (statuses[i] == IncidentStatus.DONE) {
                        incident.setResolvedAt(LocalDateTime.now().minusMinutes(30));
                    }
                    incidentRepository.save(incident);
                }
            }
        }

        // 6. 감사로그 시드 데이터
        if (auditLogRepository.count() == 0) {
            Admin admin = adminRepository.findByLoginId("admin01").orElse(null);

            if (admin != null) {
                AuditLog log1 = new AuditLog();
                log1.setAdmin(admin);
                log1.setAction(AuditAction.CREATE);
                log1.setTargetType(AuditTargetType.INCIDENT);
                log1.setTargetId(1L);
                log1.setDetail("장애 등록: [긴급] 오늘 오전 결제 연동 오류");
                auditLogRepository.save(log1);
            }
        }
    }
}