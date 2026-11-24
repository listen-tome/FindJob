package com.sky.findjob.enums;

public enum InterviewStatus {
    UPCOMING("待参加"),
    COMPLETED("已完成"),
    CANCELLED("已取消");

    private final String description;
    InterviewStatus(String description) { this.description = description; }
    public String getDescription() { return description; }
}