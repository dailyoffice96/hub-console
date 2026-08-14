package com.smconsole.inquiry.dto;

import com.smconsole.inquiry.enums.InquiryStatus;

import java.time.LocalDateTime;

public record InquiryStatusHistoryResponse(
   Long id,
   InquiryStatus beforeStatus,
   InquiryStatus afterStatus,
   String adminName,
   LocalDateTime changedAt

) { }
