package com.smconsole.incident;

import java.time.LocalDateTime;
import java.util.List;

public record IncidentDetailResponse(
        Long id,
        String title,
        String content,
        IncidentSeverity severity,
        IncidentStatus status,
        String reporter,
        LocalDateTime occurredAt,
        LocalDateTime slaDueAt,
        LocalDateTime resolvedAt,
        LocalDateTime createdAt,
        List<IncidentStatusHistoryResponse> histories
) { }
