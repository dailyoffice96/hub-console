package com.smconsole.inquiry;

public record InquiryStatsResponse (
        long waiting,
        long inProgress,
        long done
){ }
