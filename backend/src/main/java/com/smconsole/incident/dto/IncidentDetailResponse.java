package com.smconsole.incident.dto;

import com.smconsole.incident.enums.IncidentSeverity;
import com.smconsole.incident.enums.IncidentStatus;

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
        Long version,
        List<IncidentStatusHistoryResponse> histories
) { }
