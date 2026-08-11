package com.smconsole.statistics;

import com.smconsole.incident.IncidentRepository;
import com.smconsole.inquiry.InquiryRepository;
import com.smconsole.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DailyStatsService {

    private final DailyStatsRepository dailyStatsRepository;
    private final UserRepository userRepository;
    private final InquiryRepository inquiryRepository;
    private final IncidentRepository incidentRepository;

    public List<DailyStats> getDailyStats(){
        return dailyStatsRepository.findAll();
    }

    // cron = 초 / 분 / 시 / 일 / 월 / 요일
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void aggregateYesterday() {
        System.out.println("=== 일일 통계 집계 작업 시작 ===");

        LocalDate yesterday = LocalDate.now().minusDays(1);

        // User와 Inquiry의 createdAt은 LocalDate 타입이므로 LocalDate를 전달
        long newUsers = userRepository.countByCreatedAtBetween(yesterday, yesterday);
        long newInquiries = inquiryRepository.countByCreatedAtBetween(yesterday, yesterday);

        // Incident의 createdAt은 LocalDateTime 타입이므로 시간 범위를 전달
        long newIncidents = incidentRepository.countByCreatedAtBetween(
                yesterday.atStartOfDay(),
                yesterday.plusDays(1).atStartOfDay()
        );

        DailyStats dailyStats = new DailyStats();
        dailyStats.setTargetDate(yesterday.atStartOfDay());
        dailyStats.setNewUsers(newUsers);
        dailyStats.setNewInquiries(newInquiries);
        dailyStats.setNewIncidents(newIncidents);

        dailyStatsRepository.save(dailyStats);
        System.out.println("=== 일일 통계 집계 작업 완료 ===");
    }
}