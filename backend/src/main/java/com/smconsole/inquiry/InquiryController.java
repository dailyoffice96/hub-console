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

    @GetMapping("/stats")
    public ResponseEntity<InquiryStatsResponse> getStats() {
        return ResponseEntity.ok(inquiryService.getStats());
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<InquiryCommentResponse> createComment(
            @PathVariable Long id, @RequestBody CommentRequest request) {
        InquiryCommentResponse comment = inquiryService.createComment(id, request.content());
        return ResponseEntity.ok(comment);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<InquiryResponse> updateStatus(
            @PathVariable Long id, @RequestBody StatusRequest request) {
        InquiryResponse inquiry = inquiryService.updateStatus(id, request.status());
        return ResponseEntity.ok(inquiry);
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<InquiryResponse> assign(
            @PathVariable Long id, @RequestBody AssignRequest request) {
        InquiryResponse inquiry = inquiryService.updatesAssign(id, request.adminId());
        return ResponseEntity.ok(inquiry);
    }
}

