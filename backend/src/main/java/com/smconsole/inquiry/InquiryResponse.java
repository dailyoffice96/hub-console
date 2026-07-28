package com.smconsole.inquiry;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record InquiryResponse (
        Long id,
        String userName,
        String assigneeName,
        InquiryType type,
        String title,
        String content,
        InquiryStatus status,
        LocalDate createdAt,
        LocalDateTime completedAt
){}
