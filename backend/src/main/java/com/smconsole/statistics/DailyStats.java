package com.smconsole.statistics;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
// target_date에 유니크 제약을 걸어서, 애플리케이션 레벨의 "이미 있으면 스킵" 체크가 뚫리는 경쟁 상태
// (여러 인스턴스가 동시에 같은 날짜를 집계하는 경우 등)가 생겨도 DB가 최종적으로 중복을 막아준다.
@Table(name = "daily_stats", uniqueConstraints = @UniqueConstraint(name = "uk_daily_stats_target_date", columnNames = "target_date"))

public class DailyStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "daily_stats_id")
    private Long id;

    @Column(name = "target_date")
    private LocalDateTime targetDate;

    @Column(name = "new_users")
    private long newUsers;

    @Column(name = "new_inquiries")
    private long newInquiries;

    @Column(name = "new_incidents")
    private long newIncidents;

}
