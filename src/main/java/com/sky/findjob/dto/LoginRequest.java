package com.sky.findjob.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank(message = "请输入邮箱")
        @Email(message = "邮箱格式不正确")
        @Size(max = 100, message = "邮箱最多 100 个字符")
        String email,

        @NotBlank(message = "请输入密码")
        @Size(min = 6, max = 64, message = "密码长度为 6-64 位")
        String password
) {
}