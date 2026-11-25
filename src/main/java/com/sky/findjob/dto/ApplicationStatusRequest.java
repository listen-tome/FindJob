package com.sky.findjob.dto;

import com.sky.findjob.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record ApplicationStatusRequest(
        @NotNull(message = "投递状态不能为空")
        ApplicationStatus status
) {
}
