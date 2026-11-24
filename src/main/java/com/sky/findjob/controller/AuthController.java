package com.sky.findjob.controller;

import com.sky.findjob.Result;
import com.sky.findjob.dto.ChangePasswordRequest;
import com.sky.findjob.dto.LoginRequest;
import com.sky.findjob.dto.RegisterRequest;
import com.sky.findjob.dto.UpdateProfileRequest;
import com.sky.findjob.service.IAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    @Autowired
    private IAuthService authService;

    @PostMapping("/register")
    public Result register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(authService.register(request));
    }

    @PostMapping("/login")
    public Result login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @GetMapping("/me")
    public Result me() {
        return Result.success(authService.getCurrentUser());
    }
    @PutMapping("/profile")
    public Result updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return Result.success(authService.updateProfile(request));
    }
    //修改密码
    // AuthController
    @PutMapping("/password")
    public Result updatePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.updatePassword(request);
        return Result.success();
    }
}