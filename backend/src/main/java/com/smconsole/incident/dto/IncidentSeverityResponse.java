package com.smconsole.incident.dto;

public record IncidentSeverityResponse(
        long critical,
        long high,
        long medium,
        long low
) { }

