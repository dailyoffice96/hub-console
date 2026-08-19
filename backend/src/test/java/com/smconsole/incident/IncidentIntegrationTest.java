package com.smconsole.incident;

import com.jayway.jsonpath.JsonPath;
import com.smconsole.admin.entity.Admin;
import com.smconsole.admin.enums.AdminRole;
import com.smconsole.admin.repository.AdminRepository;
import com.smconsole.auditlog.AuditLogRepository;
import com.smconsole.incident.repository.IncidentRepository;
import com.smconsole.incident.repository.IncidentStatusHistoryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 장애(Incident) 도메인의 낙관적 락이 실제 HTTP 요청 두 번으로 재현되는지, 그리고
// 인증 없이 열린 웹훅 엔드포인트가 시크릿 검증을 제대로 하는지 끝까지 검증한다.
//
// 일부러 클래스에 @Transactional을 안 붙였다: 붙이면 한 테스트 메서드 안의 모든 MockMvc
// 요청이 같은 트랜잭션/영속성 컨텍스트를 공유하게 되는데, 그러면 첫 번째 상태변경으로
// 올라간 version이 flush되기 전까지 같은 영속성 컨텍스트의 두 번째 findById에는 반영되지
// 않아서(1차 캐시), 실제로는 실무에서 절대 재현 안 되는 "낙관적 락이 안 걸리는 것처럼 보이는"
// 가짜 실패가 난다. 실제 운영에서는 요청 하나당 트랜잭션이 따로 뜨기 때문에 이 문제가 없다.
// 대신 각 테스트가 끝나면 아래 cleanUp()에서 만든 데이터를 FK 순서대로 직접 지운다.
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "incident.webhook.secret=test-webhook-secret")
class IncidentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private IncidentStatusHistoryRepository incidentStatusHistoryRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    private MockHttpSession session;
    private Long testAdminId;
    private final List<Long> createdIncidentIds = new ArrayList<>();

    @BeforeEach
    void 로그인해서_세션을_확보한다() throws Exception {
        Admin admin = new Admin();
        admin.setLoginId("it_incident_admin_" + System.nanoTime());
        admin.setPasswordHash(passwordEncoder.encode("password-1234"));
        admin.setName("통합테스트관리자");
        admin.setRole(AdminRole.STAFF);
        admin = adminRepository.save(admin);
        testAdminId = admin.getId();

        MvcResult loginResult = mockMvc.perform(post("/login")
                        .param("loginId", admin.getLoginId())
                        .param("password", "password-1234"))
                .andExpect(status().isOk())
                .andReturn();

        session = (MockHttpSession) loginResult.getRequest().getSession(false);
    }

    @AfterEach
    void 만든_데이터를_정리한다() {
        for (Long incidentId : createdIncidentIds) {
            incidentStatusHistoryRepository.findByIncidentId(incidentId)
                    .forEach(incidentStatusHistoryRepository::delete);
        }
        auditLogRepository.deleteAll(
                auditLogRepository.findAll().stream()
                        .filter(log -> log.getAdmin() != null && log.getAdmin().getId().equals(testAdminId))
                        .toList()
        );
        createdIncidentIds.forEach(id -> incidentRepository.findById(id).ifPresent(incidentRepository::delete));
        adminRepository.deleteById(testAdminId);
    }

    @Test
    void 낙관적_락_버전이_다르면_상태변경이_409로_거부된다() throws Exception {
        // 1. 장애 등록
        String createBody = """
                {
                  "title": "통합테스트 장애",
                  "content": "낙관적 락 테스트용",
                  "severity": "HIGH"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/incidents")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andReturn();

        Integer id = JsonPath.read(createResult.getResponse().getContentAsString(), "$.id");
        createdIncidentIds.add(id.longValue());

        // 2. 상세 조회로 현재 버전을 읽어온다
        MvcResult detailResult = mockMvc.perform(get("/api/incidents/{id}", id).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andReturn();

        Integer currentVersion = JsonPath.read(detailResult.getResponse().getContentAsString(), "$.version");

        // 3. 정상 버전으로 상태변경 -> 200, 서버의 버전이 올라간다 (RECEIVED -> IN_PROGRESS만 허용됨)
        String validUpdateBody = String.format("""
                {"status": "IN_PROGRESS", "version": %d}
                """, currentVersion);

        mockMvc.perform(put("/api/incidents/{id}/status", id)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUpdateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        // 4. 이제 실제 버전은 올라갔는데, 방금 읽어둔 stale한 옛날 버전 값으로 다시 상태변경을 시도한다
        //    -> 이게 바로 "관리자 두 명이 동시에 같은 티켓을 고치는" 상황의 재현이다.
        String staleUpdateBody = String.format("""
                {"status": "DONE", "version": %d}
                """, currentVersion);

        mockMvc.perform(put("/api/incidents/{id}/status", id)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(staleUpdateBody))
                .andExpect(status().isConflict());
    }

    // 위 테스트와 달리 진짜로 동시에(스레드 배리어로 타이밍을 맞춰서) 5개 요청을 쏜다.
    // 이 경우 버전 비교 자체는 통과한 요청들끼리 같은 행에 UPDATE가 몰리면서 InnoDB 데드락
    // (CannotAcquireLockException)이 날 수 있는데, 이것도 결국 "동시 수정 충돌"이므로
    // 500이 아니라 409로 응답해야 한다 (GlobalExceptionHandler가 ConcurrencyFailureException을
    // 잡도록 고친 부분에 대한 회귀 테스트).
    @Test
    void 완전히_동시에_들어온_상태변경_요청은_500이_아니라_409로_처리된다() throws Exception {
        String createBody = """
                {
                  "title": "동시성 통합테스트 장애",
                  "content": "완전 동시 요청 테스트용",
                  "severity": "HIGH"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/incidents")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andReturn();

        Integer id = JsonPath.read(createResult.getResponse().getContentAsString(), "$.id");
        createdIncidentIds.add(id.longValue());

        MvcResult detailResult = mockMvc.perform(get("/api/incidents/{id}", id).session(session))
                .andExpect(status().isOk())
                .andReturn();
        Integer currentVersion = JsonPath.read(detailResult.getResponse().getContentAsString(), "$.version");

        String body = String.format("""
                {"status": "IN_PROGRESS", "version": %d}
                """, currentVersion);

        int threadCount = 5;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CyclicBarrier barrier = new CyclicBarrier(threadCount);
        List<Callable<Integer>> tasks = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            tasks.add(() -> {
                barrier.await(10, TimeUnit.SECONDS);
                return mockMvc.perform(put("/api/incidents/{id}/status", id)
                                .session(session)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andReturn().getResponse().getStatus();
            });
        }

        List<Future<Integer>> futures = pool.invokeAll(tasks, 30, TimeUnit.SECONDS);
        pool.shutdown();

        List<Integer> statusCodes = new ArrayList<>();
        for (Future<Integer> future : futures) {
            statusCodes.add(future.get());
        }

        long okCount = statusCodes.stream().filter(code -> code == 200).count();
        long conflictCount = statusCodes.stream().filter(code -> code == 409).count();
        long unexpectedCount = statusCodes.stream().filter(code -> code != 200 && code != 409).count();

        assertThat(unexpectedCount)
                .as("200/409 이외의 상태코드(주로 500)가 나오면 안 된다: " + statusCodes)
                .isZero();
        assertThat(okCount).isEqualTo(1);
        assertThat(conflictCount).isEqualTo(threadCount - 1L);
    }

    @Test
    void 웹훅_시크릿_헤더가_없으면_401을_반환한다() throws Exception {
        String body = """
                {"severity": "CRITICAL", "title": "APM 자동감지 장애", "content": "웹훅 인증 테스트"}
                """;

        mockMvc.perform(post("/api/incidents/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 웹훅_시크릿이_틀리면_401을_반환한다() throws Exception {
        String body = """
                {"severity": "CRITICAL", "title": "APM 자동감지 장애", "content": "웹훅 인증 테스트"}
                """;

        mockMvc.perform(post("/api/incidents/webhook")
                        .header("X-Webhook-Secret", "wrong-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 웹훅_시크릿이_맞으면_로그인_없이도_장애가_등록된다() throws Exception {
        String body = """
                {"severity": "CRITICAL", "title": "APM 자동감지 장애", "content": "웹훅 인증 테스트"}
                """;

        MvcResult result = mockMvc.perform(post("/api/incidents/webhook")
                        .header("X-Webhook-Secret", "test-webhook-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.severity").value("CRITICAL"))
                .andReturn();

        Integer id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        createdIncidentIds.add(id.longValue());
    }
}
