package com.smconsole.user;

// UserResponse는 이름/전화번호/이메일이 마스킹된 값("홍*동")이라, 그대로 수정 요청에
// 쓰면 마스킹 값이 원본을 덮어써버린다. 그래서 원본 값만 받는 타입을 따로 둔다.
// null/빈 값인 필드는 수정 안 하고 기존 값 유지(부분 수정 허용).
public record UserUpdateRequest(
        String name,
        String phone,
        String email
) {}
