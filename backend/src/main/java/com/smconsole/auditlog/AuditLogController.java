package com.smconsole.auditlog;

import com.smconsole.excel.AuditLogExcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/auditLogs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;
    private final AuditLogExcelService auditLogExcelService;

    @GetMapping
    public ResponseEntity<Page<AuditLogResponse>> getLog(
            @RequestParam(required = false) String adminName,
            @RequestParam(required = false) AuditTargetType targetType,
            @RequestParam(required = false) AuditAction action,
            Pageable pageable
    ){
        Page<AuditLogResponse> auditLog = auditLogService.getLog(adminName, targetType, action, pageable);
        return ResponseEntity.ok(auditLog);
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> download() {
        byte[] excelData = auditLogExcelService.exportToExcel();

        String filename = "감사로그_목록.xlsx";
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", encodedFilename);

        return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
    }

    @GetMapping("/analyze")
    public ResponseEntity<String> analyze(){
        String result = auditLogService.analyze();
        return ResponseEntity.ok(result);
    }
}

