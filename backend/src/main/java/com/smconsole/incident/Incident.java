package com.smconsole.incident;

import com.smconsole.admin.Admin;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "incidents", indexes = {
        @Index(name = "idx_incident_status", columnList = "status"),
        @Index(name = "idx_incident_severity", columnList = "severity")

})
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="incident_id")
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    private IncidentSeverity severity;

    @Enumerated(EnumType.STRING)
    private IncidentStatus status = IncidentStatus.RECEIVED;

    @ManyToOne
    @JoinColumn(name = "reporter")
    private Admin reporter;

    @Column(name = "occurred_at")
    private LocalDateTime occurredAt;

    @Column(name = "sla_due_at")
    private LocalDateTime sladueAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

}

