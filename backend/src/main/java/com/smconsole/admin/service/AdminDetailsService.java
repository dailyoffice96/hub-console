package com.smconsole.admin.service;

import com.smconsole.admin.entity.Admin;
import com.smconsole.admin.repository.AdminRepository;
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

    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        Admin admin = adminRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 직원입니다."));

        return User.builder()
                .username(admin.getLoginId())
                .password(admin.getPasswordHash())
                .authorities("ROLE_" + admin.getRole().name())
                .accountLocked(admin.isLocked())
                // 삭제(soft delete)된 계정은 이 값으로 로그인 자체를 막는다.
                .disabled(admin.isDeleted())
                .build();
    }
}

