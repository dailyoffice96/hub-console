package com.smconsole.statistics;

import com.smconsole.incident.repository.IncidentRepository;
import com.smconsole.inquiry.repository.InquiryRepository;
import com.smconsole.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

// 집계+저장 트랜잭션만 담당하는 별도 빈이다. DailyStatsService 안에 같이 두고 this.xxx()로
// 부르면 @Transactional이 프록시를 안 거쳐서 조용히 무시되기 때문에 클래스를 분리했다.
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
        // 스케줄이 중복 실행돼도 같은 날짜 데이터가 여러 번 쌓이지 않게 먼저 존재 여부를 확인한다.
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

        // 위 존재여부 체크와 save() 사이에 다시 실행될 가능성까지 막아주는 최종 방어는
        // DailyStats의 target_date 유니크 제약이 맡는다.
        dailyStatsRepository.save(dailyStats);
        return true;
    }
}
