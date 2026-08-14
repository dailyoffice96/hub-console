package com.smconsole.admin.service;

import com.smconsole.admin.entity.Admin;
import com.smconsole.admin.repository.AdminRepository;
import com.smconsole.admin.enums.AdminRole;
import com.smconsole.admin.dto.AdminCreateRequest;
import com.smconsole.admin.dto.AdminResponse;
import com.smconsole.admin.dto.AdminStatsResponse;
import com.smconsole.auditlog.AuditAction;
import com.smconsole.auditlog.AuditLogService;
import com.smconsole.auditlog.AuditTargetType;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private static final int MIN_PASSWORD_LENGTH = 4;

    private final AdminRepository adminRepository;
    private final AuditLogService auditLogService;
    private final AdminSessionService adminSessionService;
    private final PasswordEncoder passwordEncoder;

    public Page<AdminResponse> getAdmins(
            String name, String loginId, AdminRole role, Pageable pageable){
        Page<Admin> admins;

        if (loginId != null && !loginId.isEmpty()) {
            admins = adminRepository.findByLoginIdContainingAndIsDeletedFalse(loginId, pageable);
        } else if (name != null && !name.isEmpty()) {
            admins = adminRepository.findByNameContainingAndIsDeletedFalse(name, pageable);
        } else if (role != null) {
            admins = adminRepository.findByRoleAndIsDeletedFalse(role, pageable);
        } else {
            admins = adminRepository.findByIsDeletedFalse(pageable);
        }

        return admins.map(this::toResponse);
    }

    @Cacheable(value = "adminStats")
    public AdminStatsResponse getStats() {
        long total = adminRepository.countByIsDeletedFalse();
        long locked = adminRepository.countByIsLockedTrueAndIsDeletedFalse();
        long superAdmin = adminRepository.countByRoleAndIsDeletedFalse(AdminRole.SUPER_ADMIN);
        long admin = adminRepository.countByRoleAndIsDeletedFalse(AdminRole.ADMIN);
        long staff = adminRepository.countByRoleAndIsDeletedFalse(AdminRole.STAFF);

        return new AdminStatsResponse(total, locked, superAdmin, admin, staff);
    }

    @CacheEvict(value = "adminStats", allEntries = true)
    public AdminResponse unlock(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));

        // 삭제된 계정은 애초에 로그인이 안 되니(disabled 처리됨) 잠금 해제도 의미가 없다 - 막아둔다.
        // 잠겨있지 않은 계정에 다시 호출하는 건 막을 이유가 없어서 멱등하게 그냥 성공 처리한다.
        if (admin.isDeleted()) {
            throw new IllegalArgumentException("삭제된 관리자는 잠금 해제할 수 없습니다.");
        }

        admin.setLocked(false);
        admin.setLoginFailCount(0);
        adminRepository.save(admin);
        return toResponse(admin);
    }

    @CacheEvict(value = "adminStats", allEntries = true)
    public void delete(Long id) {
        Admin target = adminRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));

        // 이미 삭제된 관리자를 또 삭제하면 감사로그에 같은 삭제 이벤트가 중복으로 쌓이니 막는다.
        if (target.isDeleted()) {
            throw new IllegalArgumentException("이미 삭제된 관리자입니다.");
        }

        // 마지막 남은 SUPER_ADMIN은 삭제할 수 없다 - 삭제되면 점검모드 설정 변경, 관리자 생성 등
        // SUPER_ADMIN 전용 기능을 아무도 수행할 수 없는 상태가 되기 때문.
        if (target.getRole() == AdminRole.SUPER_ADMIN
                && adminRepository.countByRoleAndIsDeletedFalse(AdminRole.SUPER_ADMIN) <= 1) {
            throw new IllegalArgumentException("마지막 남은 SUPER_ADMIN 계정은 삭제할 수 없습니다.");
        }

        Admin currentAdmin = getCurrentAdmin();

        target.setDeleted(true);
        adminRepository.save(target);

        // 삭제된 계정으로 이미 로그인해서 남아있는 세션이 있다면 즉시 끊는다.
        adminSessionService.expireSessions(target.getLoginId());

        auditLogService.log(currentAdmin, AuditAction.DELETE, AuditTargetType.ADMIN, target.getId(),
                "관리자 계정 삭제: " + target.getName() + "(" + target.getLoginId() + ")");
    }

    @CacheEvict(value = "adminStats", allEntries = true)
    public AdminResponse create(AdminCreateRequest request) {
        if (request.loginId() == null || request.loginId().isBlank()
                || request.password() == null || request.password().isBlank()
                || request.name() == null || request.name().isBlank()
                || request.role() == null) {
            throw new IllegalArgumentException("아이디, 비밀번호, 이름, 권한을 모두 입력해 주세요.");
        }
        if (request.password().length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("비밀번호는 " + MIN_PASSWORD_LENGTH + "자 이상이어야 합니다.");
        }
        if (adminRepository.findByLoginId(request.loginId()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        Admin admin = new Admin();
        admin.setLoginId(request.loginId());
        admin.setPasswordHash(passwordEncoder.encode(request.password()));
        admin.setName(request.name());
        admin.setRole(request.role());
        adminRepository.save(admin);

        Admin currentAdmin = getCurrentAdmin();
        auditLogService.log(currentAdmin, AuditAction.CREATE, AuditTargetType.ADMIN, admin.getId(),
                "관리자 계정 생성: " + admin.getName() + "(" + admin.getLoginId() + ")");

        return toResponse(admin);
    }

    // 본인이 로그인한 상태에서 자기 비밀번호를 바꾸는 셀프서비스. 다른 사람이 남의 비밀번호를
    // 대신 바꿔주는 게 아니라, currentPassword로 본인 확인을 한 뒤에만 바뀐다.
    public void changeMyPassword(String loginId, String currentSessionId, String currentPassword, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("새 비밀번호를 입력해 주세요.");
        }
        if (newPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("비밀번호는 " + MIN_PASSWORD_LENGTH + "자 이상이어야 합니다.");
        }

        Admin me = adminRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalStateException("로그인 정보를 찾을 수 없습니다."));

        if (currentPassword == null || !passwordEncoder.matches(currentPassword, me.getPasswordHash())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }

        me.setPasswordHash(passwordEncoder.encode(newPassword));
        adminRepository.save(me);

        // 다른 기기/탭에 남아있던 세션은 끊되, 방금 이 요청을 보낸 세션은 그대로 로그인 유지시킨다.
        adminSessionService.expireSessionsExcept(me.getLoginId(), currentSessionId);

        auditLogService.log(me, AuditAction.UPDATE, AuditTargetType.ADMIN, me.getId(),
                "본인 비밀번호 변경: " + me.getName() + "(" + me.getLoginId() + ")");
    }

    private Admin getCurrentAdmin() {
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        return adminRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalStateException("로그인 정보를 찾을 수 없습니다."));
    }

    private AdminResponse toResponse(Admin admin) {
        return new AdminResponse(
                admin.getId(),
                admin.getLoginId(),
                admin.getName(),
                admin.getRole(),
                admin.isLocked(),
                admin.getCreatedAt()
        );
    }
}