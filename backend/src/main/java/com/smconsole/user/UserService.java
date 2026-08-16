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

    // 통계는 목록 화면 들어갈 때마다 호출돼서 매번 count 쿼리 3번 도는 걸 막으려고 캐싱한다.
    @Cacheable(value = "userStats")
    public UserStatsResponse  getStats(){
        long active = userRepository.countByStatus(UserStatus.ACTIVE);
        long dormant = userRepository.countByStatus(UserStatus.DORMANT);
        long withdrawn = userRepository.countByStatus(UserStatus.WITHDRAWN);
        return  new  UserStatsResponse(active, dormant, withdrawn);
    }

    // 마스킹된 값(UserResponse)이 아니라 원본 값(UserUpdateRequest)을 받는다.
    // 마스킹된 "홍*동" 같은 값을 그대로 받으면 그게 원본을 덮어써버린다.
    // null/빈 값인 필드는 수정 안 하고 기존 값 그대로 둔다.
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

    // "최근 미접속 N일" 같은 자동 판단 없이, 관리자가 호출하면 무조건 바로 DORMANT로 바뀐다.
    // 상태가 바뀌면 캐시해둔 통계 숫자도 같이 바뀌어야 해서 캐시를 지운다.
    @CacheEvict(value = "userStats", allEntries = true)
    public UserResponse dormant(Long id){
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        user.setStatus(UserStatus.DORMANT);
        user.setDormantAt(LocalDate.now());

        userRepository.save(user);
        return toResponse(user);
    }

    // dormantAt은 "휴면으로 바뀐 시점"이라 휴면 해제하면 같이 초기화한다.
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

    // 목록/검색용 toResponse()와 달리, 상세 조회에서는 마스킹 안 된 실명(name)도 같이 내려준다.
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

    // 개인정보 보호를 위해 이름 가운데 글자를 *로 가린다.
    private String maskName(String name){
        if (name == null || name.length() <= 1) return name;
        if (name.length() == 2) return  name.charAt(0) + "*";
        else return name.charAt(0) + "*".repeat(name.length() - 2) + name.charAt(name.length()-1);
    }

    // 개인정보 보호를 위해 전화번호 가운데 자리를 *로 가린다.
    private String maskPhone(String phone){
        if (phone == null) return null;
        else return phone.replaceAll("(\\d{2,3})-(\\d{3,4})-(\\d{4})", "$1-****-$3");
    }

    // 개인정보 보호를 위해 이메일 아이디 부분을 일부만 *로 가린다.
    private String maskEmail(String email){
        if (email == null) return null;
        String[] parts = email.split("@");
        String id = parts[0];
        if (id.length() <= 2) return id.charAt(0) + "*@" + parts[1];
        else return id.substring(0, 2)+ "*".repeat(id.length()-3)+ "@" + parts[1];
    }
}
