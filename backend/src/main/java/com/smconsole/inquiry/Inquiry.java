package com.smconsole.inquiry;

import com.smconsole.admin.Admin;
import com.smconsole.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "inquiries")
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private Admin assignee;

    @Enumerated(EnumType.STRING)
    private InquiryType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    private InquiryStatus status;

    @Column(name = "created_at")
    private LocalDate createdAt = LocalDate.now();

    @Column(name = "completed_at")
    private LocalDate completedAt;
}

