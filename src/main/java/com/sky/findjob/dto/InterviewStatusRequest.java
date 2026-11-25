package com.sky.findjob.dto;

import com.sky.findjob.enums.InterviewStatus;
import jakarta.validation.constraints.NotNull;

public record InterviewStatusRequest(
        @NotNull(message = "面试状态不能为空")
        InterviewStatus status
) {
}
