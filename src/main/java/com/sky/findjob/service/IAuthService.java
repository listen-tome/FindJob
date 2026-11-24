package com.sky.findjob.service;

import com.sky.findjob.dto.*;
import jakarta.validation.Valid;

public interface IAuthService {

    UserResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    UserResponse getCurrentUser();


    Object updateProfile(@Valid UpdateProfileRequest request);


    void updatePassword(@Valid ChangePasswordRequest request);
}