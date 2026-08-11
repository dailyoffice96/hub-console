package com.smconsole.incident;

public record WebhookIncidentRequest (
        IncidentSeverity severity,
        String title,
        String content
) { }
