package com.smconsole.incident;

public record IncidentSeverityResponse(
        long critical,
        long high,
        long medium,
        long low
) { }

