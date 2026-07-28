package com.smconsole.inquiry;


import com.smconsole.admin.Admin;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "inquiry_comments")
public class InquiryComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_comment_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "inquiry_id")
    private Inquiry inquiry;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private Admin admin;

    private String content;

    @Column(name = "created_at")
    private LocalDate createdAt = LocalDate.now();
}

