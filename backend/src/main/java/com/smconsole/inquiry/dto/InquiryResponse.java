package com.smconsole.inquiry.dto;

import com.smconsole.inquiry.enums.InquiryStatus;
import com.smconsole.inquiry.enums.InquiryType;

import java.time.LocalDate;

public record InquiryResponse (
        Long id,
        String userName,
        String assigneeName,
        Long assigneeId,
        InquiryType type,
        String title,
        String content,
        InquiryStatus status,
        LocalDate createdAt,
        LocalDate completedAt
){}
