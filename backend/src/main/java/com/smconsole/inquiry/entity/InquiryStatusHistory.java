package com.smconsole.inquiry.entity;

import com.smconsole.admin.entity.Admin;
import com.smconsole.inquiry.enums.InquiryStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "inquiry_status_histories")
public class InquiryStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Inquiry_history_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "inquiry_id")
    private Inquiry inquiry;

    @Enumerated(EnumType.STRING)
    @Column(name = "before_status")
    private InquiryStatus beforeStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "after_status")
    private InquiryStatus afterStatus;

    @ManyToOne
    @JoinColumn(name = "changed_by")
    private Admin changedBy;

    @Column(name = "changed_at")
    private LocalDateTime changedAt = LocalDateTime.now();

}
