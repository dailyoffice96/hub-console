package com.smconsole.incident;

import java.time.LocalDateTime;

public record IncidentStatusHistoryResponse(
     Long id,
     IncidentStatus beforeStatus,
     IncidentStatus afterStatus,
     String changedBy,
     LocalDateTime changedAt
) { }
