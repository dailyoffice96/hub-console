package com.smconsole.user;

import java.time.LocalDate;

// 단건 상세 조회 전용 응답. 목록/검색(UserResponse)은 마스킹된 이름만 내려주지만,
// 상세 화면은 관리자가 특정 회원을 콕 집어 클릭해서 들어온 경우라서 실명(name)을
// 같이 내려준다 - 검색 결과 목록에서 마스킹된 이름끼리 헷갈려 본인 확인이 안 되는
// 문제는, 이름을 해시/평문으로 바꾸는 게 아니라 "상세 화면에서만 실명 공개" 방식으로 푼다.
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
