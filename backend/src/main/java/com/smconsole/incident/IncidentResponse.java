package com.smconsole.incident;

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
