package com.company.content.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CancelScheduleRequest {
    @NotNull(message = "调度ID不能为空")
    private Long scheduleId;
}
