package com.smconsole.inquiry.dto;

import com.smconsole.inquiry.enums.InquiryStatus;

public record InquiryStatusRequest(InquiryStatus status, Long version) {}