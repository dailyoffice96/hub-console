package com.smconsole.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLoginId(String loginId);
    Page<User> findByName(String name, Pageable pageable);
    Page<User> findByNameAndPhone(String name, String phone, Pageable pageable);
    Page<User> findByStatus(UserStatus status, Pageable pageable);

}

