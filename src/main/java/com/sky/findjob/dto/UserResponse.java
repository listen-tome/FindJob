package com.sky.findjob.dto;


import com.sky.findjob.entity.User;

public record UserResponse(
        String id,
        String name,
        String email,
        String school,
        String major,
        Integer graduationYear,
        String targetRole,
        String location,
        String bio,
        Integer weeklyGoal
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                String.valueOf(user.getId()),
                user.getName(),
                user.getEmail(),
                user.getSchool(),
                user.getMajor(),
                user.getGraduationYear(),
                user.getTargetRole(),
                user.getLocation(),
                user.getBio(),
                user.getWeeklyGoal()
        );
    }
}