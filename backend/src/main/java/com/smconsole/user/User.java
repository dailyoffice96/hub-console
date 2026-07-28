package com.smconsole.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id")
    private Long id;

    @Column(name="user_name", nullable = false)
    private String name;

    @Column(name = "login_id", nullable = false, unique = true)
    private String loginId;

    private String phone;

    private String email;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Column(name="created_at")
    private LocalDate createdAt = LocalDate.now();

    @Column(name="with_drawn_at")
    private LocalDate withdrawnAt;

    @Column(name="up_date_at")
    private LocalDate updatedAt;

}

