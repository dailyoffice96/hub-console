package com.smconsole.statistics;

import com.smconsole.notification.SlackNotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.reflect.Method;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * DailyStatsService.aggregateYesterday()의 타임존 설정과, 실패/중복 시 로그·Slack 처리를 검증한다.
 * 실제 스케줄이 도는 걸 기다릴 순 없으니(자정까지 대기 불가) @Scheduled 메타데이터를 리플렉션으로
 * 직접 읽어서 확인하고, 실행 로직 자체는 메서드를 직접 호출해서 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class DailyStatsServiceTest {

    @Mock
    private DailyStatsRepository dailyStatsRepository;
    @Mock
    private DailyStatsAggregator dailyStatsAggregator;
    @Mock
    private SlackNotificationService slackNotificationService;

    private DailyStatsService dailyStatsService;

    private void setUp() {
        dailyStatsService = new DailyStatsService(dailyStatsRepository, dailyStatsAggregator, slackNotificationService);
    }

    @Test
    void Scheduled_cron이_매일_자정이고_zone이_Asia_Seoul로_명시돼_있다() throws NoSuchMethodException {
        Method method = DailyStatsService.class.getMethod("aggregateYesterday");
        Scheduled scheduled = method.getAnnotation(Scheduled.class);

        assertThat(scheduled).isNotNull();
        assertThat(scheduled.cron()).isEqualTo("0 0 0 * * *");
        assertThat(scheduled.zone()).isEqualTo("Asia/Seoul");
    }

    @Test
    void 정상_집계되면_슬랙_알림을_보내지_않는다() {
        setUp();
        when(dailyStatsAggregator.aggregate(any())).thenReturn(true);

        assertThatCode(() -> dailyStatsService.aggregateYesterday()).doesNotThrowAnyException();

        verify(dailyStatsAggregator).aggregate(LocalDate.now().minusDays(1));
        verify(slackNotificationService, never()).notification(any());
    }

    @Test
    void 이미_집계된_날짜라_건너뛴_경우도_슬랙_알림을_보내지_않는다() {
        setUp();
        when(dailyStatsAggregator.aggregate(any())).thenReturn(false);

        assertThatCode(() -> dailyStatsService.aggregateYesterday()).doesNotThrowAnyException();

        verify(slackNotificationService, never()).notification(any());
    }

    @Test
    void 집계_중_예외가_나면_예외를_삼키고_로그와_슬랙_알림을_남긴다() {
        setUp();
        when(dailyStatsAggregator.aggregate(any())).thenThrow(new RuntimeException("DB 연결 실패"));

        // 스케줄러 스레드 밖으로 예외가 전파되면 안 된다 - 이게 새면 다음 스케줄 등록 자체에 영향을
        // 줄 수 있다 (Spring이 기본적으로 잡아주긴 하지만, 서비스 자체가 흡수하는 게 목표).
        assertThatCode(() -> dailyStatsService.aggregateYesterday()).doesNotThrowAnyException();

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(slackNotificationService).notification(messageCaptor.capture());
        assertThat(messageCaptor.getValue()).contains("일일 통계 집계 실패");
        assertThat(messageCaptor.getValue()).contains("DB 연결 실패");
    }

    @Test
    void 유니크_제약_위반은_실패_알림_없이_경고로만_처리한다() {
        setUp();
        // target_date 유니크 제약 충돌 - 다른 인스턴스/재실행이 먼저 저장한 경쟁 상태로 간주하고
        // 에러 취급(Slack 알림)하지 않아야 한다.
        when(dailyStatsAggregator.aggregate(any()))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry for key 'uk_daily_stats_target_date'"));

        assertThatCode(() -> dailyStatsService.aggregateYesterday()).doesNotThrowAnyException();

        verify(slackNotificationService, never()).notification(any());
    }

    @Test
    void 집계_실패_후_슬랙_알림마저_실패해도_예외가_전파되지_않는다() {
        setUp();
        when(dailyStatsAggregator.aggregate(any())).thenThrow(new RuntimeException("DB 연결 실패"));
        org.mockito.Mockito.doThrow(new RuntimeException("Slack 웹훅도 실패"))
                .when(slackNotificationService).notification(any());

        assertThatCode(() -> dailyStatsService.aggregateYesterday()).doesNotThrowAnyException();
        verify(slackNotificationService, times(1)).notification(any());
    }
}
