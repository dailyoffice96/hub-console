package com.smconsole.inquiry;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    @GetMapping
    public ResponseEntity<Page<InquiryResponse>> getInquiry(
      @RequestParam(required = false) String assigneeName,
      @RequestParam(required = false) InquiryStatus status,
      @RequestParam(required = false) InquiryType type,
      Pageable pageable
    ){
      Page<InquiryResponse> inquiry = inquiryService.getInquiry(assigneeName, status, type, pageable);
      return ResponseEntity.ok(inquiry);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InquiryDetailResponse> getDetail(@PathVariable Long id){
        InquiryDetailResponse inquiry = inquiryService.getDetail(id);
        return ResponseEntity.ok(inquiry);
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<InquiryCommentResponse> createComment(@PathVariable Long id, @RequestBody String content){
        InquiryCommentResponse inquiry = inquiryService.createComment(id, content);
        return ResponseEntity.ok(inquiry);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<InquiryResponse> updateStatus(@PathVariable Long id, @RequestBody InquiryStatus status){
        InquiryResponse inquiry = inquiryService.updateStatus(id, status);
        return ResponseEntity.ok(inquiry);
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<InquiryResponse> updatesAssign(@PathVariable Long id, @RequestBody Long adminId){
        InquiryResponse inquiry = inquiryService.updatesAssign(id, adminId);
        return ResponseEntity.ok(inquiry);
    }
}

