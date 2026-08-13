package com.smconsole.user;

public record
UserStatsResponse (
    long active, long dormant, long withdrawn
){ }
