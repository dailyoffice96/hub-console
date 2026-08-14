package com.smconsole.incident.dto;

import com.smconsole.incident.enums.IncidentStatus;

import java.time.LocalDateTime;

public record IncidentStatusHistoryResponse(
     Long id,
     IncidentStatus beforeStatus,
     IncidentStatus afterStatus,
     String changedBy,
     LocalDateTime changedAt
) { }
