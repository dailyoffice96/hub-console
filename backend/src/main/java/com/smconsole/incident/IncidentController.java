package com.smconsole.incident;

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

    @PostMapping("/webhook")
    public ResponseEntity<IncidentResponse> createWebhook(@RequestBody WebhookIncidentRequest request){
        IncidentResponse incident = incidentService.createWebhook(request);
        return ResponseEntity.ok(incident);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<IncidentResponse> updateStatus(
            @PathVariable Long id,  @RequestBody IncidentStatusRequest request){
        IncidentResponse incident = incidentService.updateStatus(id, request.status(), request.version());
        return ResponseEntity.ok(incident);
    }
}

