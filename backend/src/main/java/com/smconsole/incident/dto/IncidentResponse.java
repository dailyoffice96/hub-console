package com.smconsole.incident.dto;

import com.smconsole.incident.enums.IncidentSeverity;
import com.smconsole.incident.enums.IncidentStatus;

import java.time.LocalDateTime;

public record IncidentResponse (
    Long id,
    String title,
    IncidentSeverity severity,
    IncidentStatus status,
    String reporter,
    LocalDateTime occurredAt,
    LocalDateTime slaDueAt
){ }
