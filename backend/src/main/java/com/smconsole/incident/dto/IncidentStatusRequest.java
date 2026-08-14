package com.smconsole.incident.dto;

import com.smconsole.incident.enums.IncidentStatus;

public record IncidentStatusRequest(IncidentStatus status, Long version) { }
