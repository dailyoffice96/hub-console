package com.smconsole.user;

// 회원 정보 수정 전용 요청 DTO. UserResponse는 이름/전화번호/이메일이 이미 마스킹된 값만
// 담고 있어서(예: "홍*동"), 이걸 그대로 수정 요청 바디로 재사용하면 화면에 표시된
// 마스킹 값이 그대로 DB 원본 데이터를 덮어써버리는 사고로 이어진다. 그래서 수정 시엔
// 마스킹되지 않은 실제 값만 받는 이 타입을 따로 둔다.
// 필드는 부분 수정을 허용한다 - null/빈 값인 필드는 기존 값을 그대로 유지한다.
public record UserUpdateRequest(
        String name,
        String phone,
        String email
) {}
