package com.smconsole.inquiry;

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
        List<InquiryCommentResponse> comments,
        List<InquiryStatusHistoryResponse> histories
) {}