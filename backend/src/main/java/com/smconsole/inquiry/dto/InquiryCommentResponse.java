package com.smconsole.inquiry.dto;

import java.time.LocalDateTime;

public record InquiryCommentResponse(
        Long id,
        String changedByName,
        String content,
        LocalDateTime createdAt
) { }
