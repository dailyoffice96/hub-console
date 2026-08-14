package com.smconsole.inquiry.dto;

public record InquiryStatsResponse (
        long waiting,
        long inProgress,
        long done
){ }
