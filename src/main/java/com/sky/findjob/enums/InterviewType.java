package com.sky.findjob.enums;

public enum InterviewType {
    PHONE("电话"),
    VIDEO("视频"),
    ONSITE("现场");

    private final String description;
    InterviewType(String description) { this.description = description; }
    public String getDescription() { return description; }
}

