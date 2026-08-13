package com.smconsole.statistics;

import com.smconsole.notification.SlackNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyStatsService {

    private final DailyStatsRepository dailyStatsRepository;
    private final DailyStatsAggregator dailyStatsAggregator;
    private final SlackNotificationService slackNotificationService;

    public List<DailyStats> getDailyStats(){
        return dailyStatsRepository.findAll();
    }

    // cron = 초 / 분 / 시 / 일 / 월 / 요일
    // zone을 명시하지 않으면 JVM 기본 타임존 기준으로 "자정"이 결정된다. 배포 호스트가 UTC로 뜨면
    // 한국 시간 자정이 아니라 오전 9시에 도는 식으로 어긋날 수 있어서 명시적으로 고정한다.
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void aggregateYesterday() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("=== 일일 통계 집계 작업 시작 (대상 날짜: {}) ===", yesterday);

        try {
            boolean aggregated = dailyStatsAggregator.aggregate(yesterday);
            if (aggregated) {
                log.info("=== 일일 통계 집계 작업 완료 (대상 날짜: {}) ===", yesterday);
            } else {
                log.warn("{} 날짜의 일일 통계가 이미 존재해 집계를 건너뜁니다.", yesterday);
            }
        } catch (DataIntegrityViolationException e) {
            // target_date 유니크 제약 위반 - 다른 인스턴스가 같은 날짜를 먼저 집계해 저장했다는 뜻일
            // 가능성이 높다. 데이터가 잘못된 게 아니라 정상적인 경쟁 상태 해소이므로 에러 알림 없이
            // 경고 로그만 남긴다.
            log.warn("{} 날짜의 일일 통계 저장이 유니크 제약에 걸렸습니다(다른 프로세스가 먼저 저장한 것으로 추정): {}",
                    yesterday, e.getMessage());
        } catch (Exception e) {
            log.error("일일 통계 집계 작업 실패 (대상 날짜: {})", yesterday, e);
            notifyFailure(yesterday, e);
            // 스케줄러 스레드 밖으로 다시 던지지 않는다 - 여기서 던져봤자 재시도가 되는 게 아니라
            // Spring 내부 로거에 스택트레이스가 한 번 더 남는 것 외엔 차이가 없고, 로그/Slack 알림은
            // 이미 위에서 남겼다.
        }
    }

    private void notifyFailure(LocalDate targetDate, Exception cause) {
        try {
            slackNotificationService.notification(
                    "⚠️ 일일 통계 집계 실패\n대상 날짜: " + targetDate + "\n원인: " + cause.getMessage()
            );
        } catch (Exception e) {
            // Slack 알림 자체가 실패해도(웹훅 URL 미설정 등) 집계 실패 처리 흐름이 죽으면 안 된다.
            log.warn("일일 통계 집계 실패 알림(Slack) 전송도 실패: {}", e.getMessage(), e);
        }
    }
}
