package com.smconsole.admin;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MyInfoController {

    private final AdminRepository adminRepository;
    private final AdminService adminService;

    // Authentication 객체 안에는, "지금 로그인한 사람에 대한 정보"가 여러 개 들어있는데,
    // 그중 getName()은 그 사람의 **"식별자(로그인 아이디)"를 돌려줌
    @GetMapping("/api/me")
    public ResponseEntity <MyInfoResponse> getInfo(Authentication authentication){
        String loginId = authentication.getName();
        Admin admin = adminRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));

        MyInfoResponse response = new MyInfoResponse(admin.getName(), admin.getRole());
        return ResponseEntity.ok(response);
    }

    // 본인 비밀번호 변경(셀프서비스). 다른 관리자의 비밀번호는 이 API로 바꿀 수 없다 -
    // Authentication에서 나오는 로그인 아이디가 곧 변경 대상이라, "누구를 바꿀지"를 요청자가 고를 수 없음.
    @PutMapping("/api/me/password")
    public ResponseEntity<Void> changeMyPassword(Authentication authentication, HttpServletRequest request,
                                                  @RequestBody AdminPasswordChangeRequest body) {
        String loginId = authentication.getName();
        String sessionId = request.getSession().getId();
        adminService.changeMyPassword(loginId, sessionId, body.currentPassword(), body.newPassword());
        return ResponseEntity.noContent().build();
    }
}
