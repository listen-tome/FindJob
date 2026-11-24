package com.sky.findjob.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "请输入姓名")
        @Size(max = 50, message = "姓名最多 50 个字符")
        String name,

        @NotBlank(message = "请输入邮箱")
        @Email(message = "邮箱格式不正确")
        String email,

        @NotBlank(message = "请输入密码")
        @Size(min = 6, max = 64, message = "密码至少 6 位")
        String password
) {
}