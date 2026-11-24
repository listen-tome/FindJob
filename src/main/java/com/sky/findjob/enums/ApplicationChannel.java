package com.sky.findjob.enums;

public enum ApplicationChannel {
    BOSS("BOSS直聘"),
    REFERRAL("内推"),
    OFFICIAL("企业官网"),
    LINKEDIN("领英"),
    CAMPUS("校园招聘"),
    OTHER("其他");

    private final String description;

    ApplicationChannel(String description) {
        this.description = description;
    }
    public String getDescription() { return description; }
}