package com.smconsole.admin;

import com.smconsole.auditlog.AuditAction;
import com.smconsole.auditlog.AuditLogService;
import com.smconsole.auditlog.AuditTargetType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final AdminRepository adminRepository;
    private final AuditLogService auditLogService;

    public Page<AdminResponse> getAdmins(
            String name, String loginId, AdminRole role, Pageable pageable){
        Page<Admin> admins;

        if (loginId != null && !loginId.isEmpty()) {
            admins = adminRepository.findByLoginIdContaining(loginId, pageable);
        } else if (name != null && !name.isEmpty()) {
            admins = adminRepository.findByNameContaining(name, pageable);
        } else if (role != null) {
            admins = adminRepository.findByRole(role, pageable);
        } else {
            admins = adminRepository.findAll(pageable);
        }

        return admins.map(this::toResponse);
    }

    public AdminStatsResponse getStats() {
        long total = adminRepository.count();
        long locked = adminRepository.countByIsLockedTrue();
        long superAdmin = adminRepository.countByRole(AdminRole.SUPER_ADMIN);
        long admin = adminRepository.countByRole(AdminRole.ADMIN);
        long staff = adminRepository.countByRole(AdminRole.STAFF);

        return new AdminStatsResponse(total, locked, superAdmin, admin, staff);
    }

    public AdminResponse unlock(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));
        admin.setLocked(false);
        admin.setLoginFailCount(0);
        adminRepository.save(admin);
        return toResponse(admin);
    }

    public void delete(Long id) {
        Admin target = adminRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));

        Admin currentAdmin = getCurrentAdmin();

        target.setDeleted(true);
        adminRepository.save(target);

        auditLogService.log(currentAdmin, AuditAction.DELETE, AuditTargetType.INQUIRY, target.getId(),
                "관리자 계정 삭제: " + target.getName() + "(" + target.getLoginId() + ")");


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