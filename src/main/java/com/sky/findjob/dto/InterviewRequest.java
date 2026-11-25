package com.sky.findjob.dto;

import com.sky.findjob.enums.InterviewStatus;
import com.sky.findjob.enums.InterviewType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * 新增 / 更新面试的请求体。
 */
public record InterviewRequest(
        String applicationId,

        @Size(max = 40, message = "公司名称最多 40 个字符")
        String company,

        @Size(max = 60, message = "岗位名称最多 60 个字符")
        String position,

        @NotBlank(message = "面试轮次不能为空")
        @Size(max = 50, message = "面试轮次最多 50 个字符")
        String round,

        @NotNull(message = "面试形式不能为空")
        InterviewType type,

        @NotNull(message = "面试时间不能为空")
        LocalDateTime scheduledAt,

        @Min(value = 1, message = "面试时长至少 1 分钟")
        Integer durationMinutes,

        @Size(max = 100, message = "面试地点最多 100 个字符")
        String location,

        @Size(max = 500, message = "会议链接最多 500 个字符")
        String link,

        @Size(max = 100, message = "面试官最多 100 个字符")
        String interviewer,

        InterviewStatus status,

        String notes
) {
}
