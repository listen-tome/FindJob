package com.sky.findjob.dto;

import com.sky.findjob.enums.ApplicationChannel;

import java.util.List;

public record AnalyticsResponse(
        String range,
        Summary summary,
        List<TrendPoint> trend,
        List<ChannelStat> channels,
        List<StatusCount> statusDistribution,
        List<WeekdayStat> weekdays
) {
    public record Summary(
            MetricValue applications,
            MetricValue responseRate,
            MetricValue interviewRate,
            MetricValue offerRate,
            MetricValue avgResponseDays
    ) {
    }

    public record ChannelStat(
            ApplicationChannel channel,
            long applications,
            long interviews,
            long offers,
            double responseRate
    ) {
    }

    public record WeekdayStat(String label, long value) {
    }
}
