package com.smconsole.inquiry.entity;

import com.smconsole.admin.entity.Admin;
import com.smconsole.inquiry.enums.InquiryStatus;
import com.smconsole.inquiry.enums.InquiryType;
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
@Table(name = "inquiries", indexes = {
        @Index(name = "idx_inquiry_admin_id", columnList = "admin_id"),
        @Index(name = "idx_inquiry_status", columnList = "status"),
        @Index(name = "idx_inquiry_type", columnList = "type")
})
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_id")
    private Long id;

    @Version
    private Long version;

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

