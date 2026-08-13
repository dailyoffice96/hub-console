package com.smconsole.admin;

// 본인이 자기 비밀번호를 바꿀 때 쓰는 요청 DTO. currentPassword로 본인 확인을 한 뒤에만 바꿔준다.
public record AdminPasswordChangeRequest (
    String currentPassword,
    String newPassword
){ }
