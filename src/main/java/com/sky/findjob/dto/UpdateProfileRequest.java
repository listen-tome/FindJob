package com.sky.findjob.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(

        @NotBlank(message = "请输入姓名")
        @Size(max = 50, message = "姓名最多 50 个字符")
        String name,

        @Size(max = 100, message = "学校最多 100 个字符")
        String school,

        @Size(max = 100, message = "专业最多 100 个字符")
        String major,

        @Min(value = 1900, message = "毕业年份不合法")
        @Max(value = 2100, message = "毕业年份不合法")
        Integer graduationYear,

        @Size(max = 100, message = "目标岗位最多 100 个字符")
        String targetRole,

        @Size(max = 100, message = "期望城市最多 100 个字符")
        String location,

        @Size(max = 500, message = "个人简介最多 500 个字符")
        String bio,

        @Min(value = 1, message = "每周目标至少 1")
        @Max(value = 100, message = "每周目标最多 100")
        Integer weeklyGoal
) {
}