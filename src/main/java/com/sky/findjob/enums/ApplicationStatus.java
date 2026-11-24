package com.sky.findjob.enums;

public enum ApplicationStatus {
    WISHLIST("想投递", "尚未投递，可没有 appliedAt"),
    APPLIED("已投递", "简历已送达"),
    SCREENING("筛选中", "HR / 业务评估中"),
    INTERVIEW("面试中", "已进入面试流程"),
    OFFER("已获 Offer", "已发 Offer"),
    REJECTED("未通过", "流程结束");

    private final String description; // 中文描述
    private final String remark;      // 说明

    ApplicationStatus(String description, String remark) {
        this.description = description;
        this.remark = remark;
    }

    public String getDescription() {
        return description;
    }

    public String getRemark() {
        return remark;
    }
}