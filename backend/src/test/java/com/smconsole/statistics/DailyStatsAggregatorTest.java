package com.smconsole.statistics;

import com.smconsole.incident.IncidentRepository;
import com.smconsole.inquiry.InquiryRepository;
import com.smconsole.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** target_date 중복 방지(사전 체크)와 @Transactional 경계가 제대로 붙어 있는지 검증한다. */
@ExtendWith(MockitoExtension.class)
class DailyStatsAggregatorTest {

    @Mock
    private DailyStatsRepository dailyStatsRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private InquiryRepository inquiryRepository;
    @Mock
    private IncidentRepository incidentRepository;

    private DailyStatsAggregator aggregator;

    private void setUp() {
        aggregator = new DailyStatsAggregator(dailyStatsRepository, userRepository, inquiryRepository, incidentRepository);
    }

    @Test
    void aggregate_메서드에_Transactional이_붙어있다() throws NoSuchMethodException {
        // self-invocation(this.aggregate())으로 호출되면 @Transactional이 프록시를 안 타고 조용히
        // 무시되는 문제가 있었어서, 별도 빈으로 분리했다 - 그 전제인 "이 메서드엔 @Transactional이
        // 붙어 있다"를 명시적으로 확인해둔다.
        Method method = DailyStatsAggregator.class.getDeclaredMethod("aggregate", LocalDate.class);
        assertThat(method.isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class)).isTrue();
    }

    @Test
    void 이미_해당_날짜_통계가_있으면_집계하지_않고_false를_반환한다() {
        setUp();
        LocalDate target = LocalDate.of(2026, 8, 10);
        DailyStats existing = new DailyStats();
        existing.setId(1L);
        when(dailyStatsRepository.findByTargetDate(target.atStartOfDay())).thenReturn(Optional.of(existing));

        boolean result = aggregator.aggregate(target);

        assertThat(result).isFalse();
        // 이미 있으니 집계 쿼리도, 저장도 아예 안 나가야 한다
        verify(userRepository, never()).countByCreatedAtBetween(any(), any());
        verify(inquiryRepository, never()).countByCreatedAtBetween(any(), any());
        verify(incidentRepository, never()).countByCreatedAtBetween(any(), any());
        verify(dailyStatsRepository, never()).save(any());
    }

    @Test
    void 해당_날짜_통계가_없으면_집계해서_저장하고_true를_반환한다() {
        setUp();
        LocalDate target = LocalDate.of(2026, 8, 10);
        when(dailyStatsRepository.findByTargetDate(target.atStartOfDay())).thenReturn(Optional.empty());
        when(userRepository.countByCreatedAtBetween(target, target)).thenReturn(3L);
        when(inquiryRepository.countByCreatedAtBetween(target, target)).thenReturn(2L);
        when(incidentRepository.countByCreatedAtBetween(target.atStartOfDay(), target.plusDays(1).atStartOfDay()))
                .thenReturn(1L);

        boolean result = aggregator.aggregate(target);

        assertThat(result).isTrue();
        verify(dailyStatsRepository).save(org.mockito.ArgumentMatchers.argThat(stats ->
                stats.getTargetDate().equals(target.atStartOfDay())
                        && stats.getNewUsers() == 3
                        && stats.getNewInquiries() == 2
                        && stats.getNewIncidents() == 1
        ));
    }
}
