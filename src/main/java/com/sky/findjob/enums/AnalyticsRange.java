package com.sky.findjob.enums;

import java.time.LocalDate;

public enum AnalyticsRange {
    LAST_30_DAYS("30d", 30),
    LAST_90_DAYS("90d", 90),
    LAST_365_DAYS("365d", 365);

    private final String code; // 对外暴露的值（比如给前端的参数）
    private final int days;    // 包含的具体天数

    AnalyticsRange(String code, int days) {
        this.code = code;
        this.days = days;
    }

    public String getCode() { return code; }
    public int getDays() { return days; }

    // 甚至可以在这里直接写业务方法：获取对应的开始日期
    public LocalDate getStartDate() {
        return LocalDate.now().minusDays(this.days);
    }
}