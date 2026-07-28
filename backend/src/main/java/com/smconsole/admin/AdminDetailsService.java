package com.smconsole.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminDetailsService implements UserDetailsService{

    private final AdminRepository adminRepository;

    // 로그인한 아이디가 DB에 있는지 확인
    //Spring Security가 로그인 시 자동으로 이 메서드를 호출함
    //원본 인터페이스(UserDetailsService)를 구현(implements)하는 것
    //throws는 "예외가 날 수 있다"는 걸 암시적으로 알려주는 것
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        Admin admin = adminRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 직원입니다."));

        //Spring Security가 "로그인 정보를 담는 표준 상자"
        return User.builder()
                .username(admin.getLoginId())      // 직원의 아이디
                .password(admin.getPasswordHash())       // 암호화된 비밀번호
                .authorities("ROLE_" + admin.getRole().name())     // 권한
                .accountLocked(admin.isLocked())   // 계정 잠김
                .build();
    }
}

