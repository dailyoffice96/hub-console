package com.smconsole.user;

import java.time.LocalDate;

//DTO가 있어야, 그 사이에서 "원본 → 마스킹된 값"으로 바꿔치기 가능함
public record UserResponse (
        Long id,
        String loginId,
        String maskedName,
        String maskedPhone,
        String maskedEmail,
        UserStatus status,
        LocalDate createdAt,
        LocalDate withdrawnAt,
        LocalDate updatedAt
){}

