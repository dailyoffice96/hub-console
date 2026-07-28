package com.smconsole.inquiry;

import java.time.LocalDateTime;

public record InquiryStatusHistoryResponse(
   Long id,
   InquiryStatus beforeStatus,
   InquiryStatus afterStatus,
   String adminName,
   LocalDateTime changedAt

) { }
