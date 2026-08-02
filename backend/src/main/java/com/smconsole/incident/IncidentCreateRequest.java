package com.smconsole.incident;

import java.time.LocalDateTime;

public record IncidentCreateRequest(
        String title,
        String content,
        IncidentSeverity severity,
        LocalDateTime occurredAt,
        LocalDateTime slaDueAt
) {}
