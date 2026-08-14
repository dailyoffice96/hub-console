package com.smconsole.user;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import java.time.LocalDate;


@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;

    public Page<UserResponse> getSearch(
            String name, String phone, String loginId,
            UserStatus status, Pageable pageable){
        Page<User> users;

        if (loginId != null && !loginId.isEmpty()) {
            users = userRepository.findByLoginIdContaining(loginId, pageable);
        }else if (name != null && phone != null && !name.isEmpty() && !phone.isEmpty()) {
            users = userRepository.findByNameAndPhone(name, phone, pageable);
        } else if (name != null && !name.isEmpty()) {
            users = userRepository.findByName(name, pageable);
        } else if (status != null && !status.toString().isEmpty()) {
            users = userRepository.findByStatus(status, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        return users.map(this::toResponse);
    }

    public UserDetailResponse getUser(Long id) {
        User user = userRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        return toDetailResponse(user);
    }

    @Cacheable(value = "userStats")
    public UserStatsResponse  getStats(){
        long active = userRepository.countByStatus(UserStatus.ACTIVE);
        long dormant = userRepository.countByStatus(UserStatus.DORMANT);
        long withdrawn = userRepository.countByStatus(UserStatus.WITHDRAWN);
        return  new  UserStatsResponse(active, dormant, withdrawn);
    }

    // 부분 수정: null/빈 값인 필드는 기존 값을 그대로 유지한다.
    // UserResponse(마스킹된 값)가 아니라 UserUpdateRequest(원본 값)를 받는 이유는
    // toResponse()가 응답에 마스킹된 이름/전화번호/이메일만 담아서 돌려주는데, 그걸 그대로
    // 수정 요청에 재사용하면 화면에 표시된 "홍*동" 같은 마스킹 값이 그대로 DB 원본을
    // 덮어써버리기 때문이다.
    public UserResponse update(Long id, UserUpdateRequest request){
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name());
        }
        if (request.phone() != null && !request.phone().isBlank()) {
            user.setPhone(request.phone());
        }
        if (request.email() != null && !request.email().isBlank()) {
            user.setEmail(request.email());
        }
        user.setUpdatedAt(LocalDate.now());

        userRepository.save(user);
        return toResponse(user);
    }

    // 즉시 휴면 전환: 별도 유예 기간이나 "최근 미접속 N일" 같은 자동 판단 조건 없이,
    // 관리자가 이 API를 호출하는 즉시 무조건 DORMANT로 바뀌는 수동 처리입니다.
    // (이 서비스는 회원이 직접 로그인하는 구조가 아니라서 탈퇴(WITHDRAWN) 상태와의 충돌은 고려하지 않습니다.)
    @CacheEvict(value = "userStats", allEntries = true)
    public UserResponse dormant(Long id){
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        user.setStatus(UserStatus.DORMANT);
        user.setDormantAt(LocalDate.now());

        userRepository.save(user);
        return toResponse(user);
    }

    // 휴면 해제: 휴면(DORMANT) 상태를 다시 정상(ACTIVE)으로 되돌립니다.
    // dormantAt은 "휴면으로 전환된 시점"이므로 해제하면서 초기화합니다.
    @CacheEvict(value = "userStats", allEntries = true)
    public UserResponse activate(Long id){
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        user.setStatus(UserStatus.ACTIVE);
        user.setDormantAt(null);
        user.setUpdatedAt(LocalDate.now());

        userRepository.save(user);
        return toResponse(user);
    }

    //User(원본, DB 데이터)를 UserResponse(화면에 보낼 안전한 버전)로 바꾸는 작업
    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getLoginId(),
                maskName(user.getName()),
                maskPhone(user.getPhone()),
                maskEmail(user.getEmail()),
                user.getStatus(),
                user.getCreatedAt(),
                user.getDormantAt(),
                user.getUpdatedAt()
        );
    }

    // 상세 조회 전용: 목록/검색용 toResponse()와 달리 마스킹 안 된 실명(name)을 같이 담는다.
    private UserDetailResponse toDetailResponse(User user) {
        return new UserDetailResponse(
                user.getId(),
                user.getLoginId(),
                maskName(user.getName()),
                user.getName(),
                maskPhone(user.getPhone()),
                maskEmail(user.getEmail()),
                user.getStatus(),
                user.getCreatedAt(),
                user.getDormantAt(),
                user.getUpdatedAt()
        );
    }

    private String maskName(String name){
        if (name == null || name.length() <= 1) return name;
        if (name.length() == 2) return  name.charAt(0) + "*";
        else return name.charAt(0) + "*".repeat(name.length() - 2) + name.charAt(name.length()-1);
    }

    private String maskPhone(String phone){
        if (phone == null) return null;
        else return phone.replaceAll("(\\d{2,3})-(\\d{3,4})-(\\d{4})", "$1-****-$3");
    }

    private String maskEmail(String email){
        if (email == null) return null;
        String[] parts = email.split("@");
        String id = parts[0];
        if (id.length() <= 2) return id.charAt(0) + "*@" + parts[1];
        else return id.substring(0, 2)+ "*".repeat(id.length()-3)+ "@" + parts[1];
    }
}
