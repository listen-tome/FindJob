package com.sky.findjob.dto;

import com.sky.findjob.enums.ApplicationChannel;
import com.sky.findjob.enums.ApplicationPriority;
import com.sky.findjob.enums.ApplicationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/**
 * 新增 / 更新投递的请求体。
 */
public record ApplicationSaveRequest(
        @NotBlank(message = "公司名称不能为空")
        @Size(max = 40, message = "公司名称最多 40 个字符")
        String company,

        @NotBlank(message = "岗位名称不能为空")
        @Size(max = 60, message = "岗位名称最多 60 个字符")
        String position,

        @Size(max = 100, message = "地点最多 100 个字符")
        String location,

        @Size(max = 50, message = "薪资最多 50 个字符")
        String salary,

        @NotNull(message = "投递渠道不能为空")
        ApplicationChannel channel,

        @NotNull(message = "投递状态不能为空")
        ApplicationStatus status,

        @NotNull(message = "优先级不能为空")
        ApplicationPriority priority,

        LocalDate appliedAt,

        LocalDate nextActionAt,

        List<String> tags,

        String notes,

        String resumeId
) {
}
