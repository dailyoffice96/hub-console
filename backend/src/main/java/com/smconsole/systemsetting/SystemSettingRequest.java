package com.smconsole.systemsetting;

import java.time.LocalDate;

public record SystemSettingRequest(
       String message,
       LocalDate startAt,
       LocalDate endAt
) { }
