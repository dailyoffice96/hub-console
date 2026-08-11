package com.smconsole.statistics;

import com.smconsole.incident.IncidentRepository;
import com.smconsole.inquiry.InquiryRepository;
import com.smconsole.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    public List<DailyStats> aggregateYesterday() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        long newUsers = userRepository.countByCreatedAtBetween(
                yesterday.atStartOfDay(),
                yesterday.plusDays(1).atStartOfDay()
        );
        long newInquiries = inquiryRepository.countByCreatedAtBetween(
                yesterday.atStartOfDay(),
                yesterday.plusDays(1).atStartOfDay()
        );
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


        return dailyStatsRepository.findAll();

    }
}
