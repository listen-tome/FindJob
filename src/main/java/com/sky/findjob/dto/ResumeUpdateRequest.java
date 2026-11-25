package com.sky.findjob.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResumeUpdateRequest(
        @NotBlank(message = "简历名称不能为空")
        @Size(max = 100, message = "简历名称最多 100 个字符")
        String name,

        @NotBlank(message = "目标岗位不能为空")
        @Size(max = 100, message = "目标岗位最多 100 个字符")
        String targetRole
) {
}
