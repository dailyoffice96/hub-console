package com.smconsole.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByLoginId(String loginId);

    // 목록/통계 조회는 삭제(soft delete)된 관리자를 제외한다.
    // findByLoginId는 그대로 두는데, DataInitializer의 시드 중복 체크나 "현재 로그인한 사람 조회" 용도로
    // 여러 곳에서 쓰이고 있어서 여기서 필터링하면 안 되기 때문. 삭제된 계정 로그인 차단은
    // AdminDetailsService에서 UserDetails.disabled로 처리한다.
    Page<Admin> findByIsDeletedFalse(Pageable pageable);
    Page<Admin> findByNameContainingAndIsDeletedFalse(String name, Pageable pageable);
    Page<Admin> findByLoginIdContainingAndIsDeletedFalse(String loginId, Pageable pageable);
    Page<Admin> findByRoleAndIsDeletedFalse(AdminRole role, Pageable pageable);
    // 다른 도메인(문의 담당자 배정 등)에서 "삭제 안 된 관리자에게만" id로 대상을 찾을 때 사용
    Optional<Admin> findByIdAndIsDeletedFalse(Long id);

    long countByIsDeletedFalse();
    long countByRoleAndIsDeletedFalse(AdminRole role);
    long countByIsLockedTrueAndIsDeletedFalse();
}

