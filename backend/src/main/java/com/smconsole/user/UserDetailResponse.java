package com.smconsole.user;

import java.time.LocalDate;

// 목록/검색(UserResponse)은 마스킹된 이름만 내려주지만, 여긴 관리자가 특정 회원을
// 콕 집어 들어온 상세 화면이라 실명(name)도 같이 내려준다.
public record UserDetailResponse(
        Long id,
        String loginId,
        String maskedName,
        String name,
        String maskedPhone,
        String maskedEmail,
        UserStatus status,
        LocalDate createdAt,
        LocalDate dormantAt,
        LocalDate updatedAt
){}
