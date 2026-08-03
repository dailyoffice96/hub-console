package com.smconsole.systemsetting;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/systemSettings")
@RequiredArgsConstructor
public class SystemSettingController {

    private final SystemSettingService systemSettingService;

    @GetMapping
    public ResponseEntity<SystemSetting> showSystem(){
        SystemSetting systemSetting = systemSettingService.showSystem();
        return ResponseEntity.ok(systemSetting);
    }

    @PutMapping
    public ResponseEntity<SystemSetting> saveSetting(
            @RequestBody SystemSettingRequest request){
        SystemSetting systemSetting = systemSettingService.saveSetting(request);
        return ResponseEntity.ok(systemSetting);
    }
}

