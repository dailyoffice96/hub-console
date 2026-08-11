package com.smconsole.statistics;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dailyStats")
@RequiredArgsConstructor

public class DailyStatsController {

    private final DailyStatsService dailyStatsService;

    @GetMapping
    public ResponseEntity<List<DailyStats>> getDailyStats(){
        List<DailyStats> dailyStats = dailyStatsService.getDailyStats();
        return ResponseEntity.ok(dailyStats);
    }

    @GetMapping("/check")
    public ResponseEntity<List<DailyStats>> aggregateYesterday(){
        List<DailyStats> dailyStats = dailyStatsService.aggregateYesterday();
        return ResponseEntity.ok(dailyStats);
    }
}
