package com.smconsole.incident.controller;

import com.smconsole.incident.dto.*;
import com.smconsole.incident.service.IncidentService;
import com.smconsole.incident.enums.IncidentSeverity;
import com.smconsole.incident.enums.IncidentStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    @GetMapping
    public ResponseEntity<Page<IncidentResponse>> getIncident(
        @RequestParam(required = false) String reporterName,
        @RequestParam(required = false) IncidentStatus status,
        @RequestParam(required = false) IncidentSeverity severity,
        @PageableDefault(size = 9) Pageable pageable
    ){
        Page<IncidentResponse> incident = incidentService.getIncident(reporterName, status, severity, pageable);
        return ResponseEntity.ok(incident);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidentDetailResponse> getDetail(@PathVariable Long id){
        IncidentDetailResponse incident = incidentService.getDetail(id);
        return ResponseEntity.ok(incident);
    }

    @GetMapping("/stats")
    public ResponseEntity<IncidentStatsResponse> getStats(){
        return ResponseEntity.ok(incidentService.getStats());
    }

    @GetMapping("/stats/severity")
    public ResponseEntity<IncidentSeverityResponse> getSeverityStats() {
        return ResponseEntity.ok(incidentService.getSeverityStats());
    }

    @PostMapping
    public ResponseEntity<IncidentResponse> createIncident(@RequestBody IncidentCreateRequest request) {
        IncidentResponse incident = incidentService.createIncident(request);
        return ResponseEntity.ok(incident);
    }

    // 인증 없이 열려있는 엔드포인트(SecurityConfig 참고) - X-Webhook-Secret 헤더로 호출자를 검증한다.
    @PostMapping("/webhook")
    public ResponseEntity<IncidentResponse> createWebhook(
            @Valid @RequestBody WebhookIncidentRequest request,
            @RequestHeader(value = "X-Webhook-Secret", required = false) String webhookSecret
    ){
        IncidentResponse incident = incidentService.createWebhook(request, webhookSecret);
        return ResponseEntity.ok(incident);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<IncidentResponse> updateStatus(
            @PathVariable Long id,  @RequestBody IncidentStatusRequest request){
        IncidentResponse incident = incidentService.updateStatus(id, request.status(), request.version());
        return ResponseEntity.ok(incident);
    }
}
