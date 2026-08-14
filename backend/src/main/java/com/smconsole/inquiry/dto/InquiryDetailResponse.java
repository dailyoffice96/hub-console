package com.smconsole.inquiry.dto;

import com.smconsole.inquiry.enums.InquiryStatus;
import com.smconsole.inquiry.enums.InquiryType;

import java.time.LocalDate;
import java.util.List;

public record InquiryDetailResponse(
        Long id,
        String userName,
        String assigneeName,
        Long assigneeId,
        InquiryType type,
        String title,
        String content,
        InquiryStatus status,
        LocalDate createdAt,
        LocalDate completedAt,
        Long version,
        List<InquiryCommentResponse> comments,
        List<InquiryStatusHistoryResponse> histories
) {}