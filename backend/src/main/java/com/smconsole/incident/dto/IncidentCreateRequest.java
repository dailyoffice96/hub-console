package com.smconsole.incident.dto;

import com.smconsole.incident.enums.IncidentSeverity;

import java.time.LocalDateTime;

public record IncidentCreateRequest(
        String title,
        String content,
        IncidentSeverity severity,
        LocalDateTime occurredAt,
        LocalDateTime slaDueAt
) {}
