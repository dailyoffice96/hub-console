package com.smconsole.statistics;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface DailyStatsRepository extends JpaRepository<DailyStats, Long> {

    Optional<DailyStats> findByTargetDate(LocalDateTime targetDate);

}
