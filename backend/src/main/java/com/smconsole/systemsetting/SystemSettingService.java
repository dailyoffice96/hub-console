package com.smconsole.systemsetting;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SystemSettingService {
    private final SystemSettingRepository systemSettingRepository;

    public SystemSetting showSystem(){
        List<SystemSetting> all = systemSettingRepository.findAll();
            if (all.isEmpty()) {
                return null;
            }
            return all.get(0);
    }

    // 점검 설정은 항상 딱 한 행만 유지한다 - 있으면 덮어쓰고, 없으면 그때 새로 만든다.
    public SystemSetting saveSetting(SystemSettingRequest request) {
        List<SystemSetting> all = systemSettingRepository.findAll();
        SystemSetting setting;

        if (all.isEmpty()) {
            setting = new SystemSetting();
        } else {
            setting = all.get(0);
        }

        setting.setMessage(request.message());
        setting.setStartAt(request.startAt());
        setting.setEndAt(request.endAt());

        return systemSettingRepository.save(setting);
    }
}

