package com.sky.findjob.dto;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresIn,
        UserResponse user
) {
    public static LoginResponse of(String token, long expireMillis, UserResponse user) {
        return new LoginResponse(token, "Bearer", expireMillis / 1000, user);
    }
}