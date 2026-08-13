package com.smconsole.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping
    public ResponseEntity<Page<AdminResponse>> getAdmins(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String loginId,
            @RequestParam(required = false) AdminRole role,
            Pageable pageable
    ) {
        return ResponseEntity.ok(adminService.getAdmins(name, loginId, role, pageable));
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    @PutMapping("/{id}/unlock")
    public ResponseEntity<AdminResponse> unlock(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.unlock(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        adminService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<AdminResponse> create(@RequestBody AdminCreateRequest request) {
        AdminResponse admin = adminService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(admin);
    }

}
