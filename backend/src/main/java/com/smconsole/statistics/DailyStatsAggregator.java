package com.smconsole.statistics;

import com.smconsole.incident.repository.IncidentRepository;
import com.smconsole.inquiry.repository.InquiryRepository;
import com.smconsole.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 실제 집계+저장 트랜잭션만 담당한다. DailyStatsService와 분리해둔 이유:
 * @Scheduled로 호출되는 DailyStatsService#aggregateYesterday()가 같은 클래스의 @Transactional
 * 메서드를 this.xxx()로 직접 호출(self-invocation)하면 Spring AOP 프록시를 안 거치게 되어
 * @Transactional이 조용히 무시된다. 별도 빈으로 분리해서 프록시를 통한 호출이 되도록 한다.
 */
@Component
@RequiredArgsConstructor
class DailyStatsAggregator {

    private final DailyStatsRepository dailyStatsRepository;
    private final UserRepository userRepository;
    private final InquiryRepository inquiryRepository;
    private final IncidentRepository incidentRepository;

    /** @return 실제로 집계해서 저장했으면 true, 이미 그 날짜 데이터가 있어 건너뛰었으면 false */
    @Transactional
    boolean aggregate(LocalDate targetDate) {
        // 저장 전에 해당 날짜가 이미 있는지 확인 - 스케줄이 중복 실행돼도(재기동, 여러 인스턴스 등)
        // daily_stats에 같은 날짜 행이 여러 개 쌓이지 않도록 하는 애플리케이션 레벨 방어.
        // (실제 로컬 dev DB에서 이 체크가 없어 같은 날짜 행이 161개 중복 생성된 걸 확인했다.)
        if (dailyStatsRepository.findByTargetDate(targetDate.atStartOfDay()).isPresent()) {
            return false;
        }

        // User와 Inquiry의 createdAt은 LocalDate 타입이므로 LocalDate를 전달
        long newUsers = userRepository.countByCreatedAtBetween(targetDate, targetDate);
        long newInquiries = inquiryRepository.countByCreatedAtBetween(targetDate, targetDate);

        // Incident의 createdAt은 LocalDateTime 타입이므로 시간 범위를 전달
        long newIncidents = incidentRepository.countByCreatedAtBetween(
                targetDate.atStartOfDay(),
                targetDate.plusDays(1).atStartOfDay()
        );

        DailyStats dailyStats = new DailyStats();
        dailyStats.setTargetDate(targetDate.atStartOfDay());
        dailyStats.setNewUsers(newUsers);
        dailyStats.setNewInquiries(newInquiries);
        dailyStats.setNewIncidents(newIncidents);

        // 위 findByTargetDate 체크와 이 save() 사이에는 여전히 경쟁 구간이 있다(두 인스턴스가 동시에
        // 통과할 수 있음) - 그 마지막 방어는 DailyStats의 target_date 유니크 제약이 맡는다.
        // 제약에 걸리면 DataIntegrityViolationException이 던져지고, 호출자(DailyStatsService)가
        // 이를 "다른 프로세스가 먼저 저장함"으로 구분해서 처리한다.
        dailyStatsRepository.save(dailyStats);
        return true;
    }
}
