package com.smconsole.admin.dto;

import com.smconsole.admin.enums.AdminRole;

public record MyInfoResponse (
    String name,
    AdminRole role
) {}
