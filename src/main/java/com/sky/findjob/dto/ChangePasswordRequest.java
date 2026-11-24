package com.sky.findjob.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(

        @JsonAlias({"currentPassword", "oldPwd"})
        @NotBlank(message = "请输入原密码")
        String oldPassword,

        @NotBlank(message = "请输入新密码")
        @Size(min = 6, max = 64, message = "新密码长度需在 6~64 位之间")
        String newPassword,

        @JsonAlias({"confirmPwd", "rePassword", "repeatPassword", "newPassword2"})
        String confirmPassword
) {
}