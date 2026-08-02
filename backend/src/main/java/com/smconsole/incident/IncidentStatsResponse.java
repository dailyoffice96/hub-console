package com.smconsole.incident;

public record IncidentStatsResponse(
    long received,
    long inProgress,
    long done
) { }