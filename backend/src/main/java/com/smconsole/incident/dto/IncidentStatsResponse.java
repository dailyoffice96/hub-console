package com.smconsole.incident.dto;

public record IncidentStatsResponse(
    long received,
    long inProgress,
    long done
) { }