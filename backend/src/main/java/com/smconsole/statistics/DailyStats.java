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
@Table(name = "daily_stats")

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
