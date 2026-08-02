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
@Table(name = "incidents_status_histories")
public class IncidentStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="incident_history_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "incident_id")
    private Incident incident;

    @Enumerated(EnumType.STRING)
    @Column(name = "before_status")
    private IncidentStatus beforeStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "after_status")
    private IncidentStatus afterStatus;

    @ManyToOne
    @JoinColumn(name = "changed_by")
    private Admin changedBy;

    @Column(name = "changed_at")
    private LocalDateTime changedAt = LocalDateTime.now();
}

