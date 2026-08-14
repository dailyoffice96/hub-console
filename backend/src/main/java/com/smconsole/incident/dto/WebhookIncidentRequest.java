package com.smconsole.incident.dto;

import com.smconsole.incident.enums.IncidentSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WebhookIncidentRequest(
        @NotNull(message = "심각도는 필수입니다.")
        IncidentSeverity severity,

        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 255, message = "제목은 255자를 넘을 수 없습니다.")
        String title,

        @NotBlank(message = "내용은 필수입니다.")
        @Size(max = 255, message = "내용은 255자를 넘을 수 없습니다.")
        String content
) { }
