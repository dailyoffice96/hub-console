package com.smconsole.inquiry;

import java.time.LocalDateTime;

public record InquiryCommentResponse(
        Long id,
        String changedByName,
        String content,
        LocalDateTime createdAt
) { }
