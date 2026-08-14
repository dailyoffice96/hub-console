package com.smconsole.admin.dto;

public record AdminStatsResponse (
    long totalCount,
    long lockedCount,
    long superAdminCount,
    long adminCount,
    long staffCount
){ }
