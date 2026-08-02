package com.smconsole.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByLoginId(String loginId);
    Page<Admin> findByNameContaining(String name, Pageable pageable);
    Page<Admin> findByLoginIdContaining(String loginId, Pageable pageable);
    Page<Admin> findByRole(AdminRole role, Pageable pageable);

    long countByRole(AdminRole role);
    long countByIsLockedTrue();
}

