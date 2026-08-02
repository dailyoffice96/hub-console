package com.smconsole.admin;

public record AdminStatsResponse (
    long totalCount,
    long lockedCount,
    long superAdminCount,
    long adminCount,
    long staffCount
){ }
