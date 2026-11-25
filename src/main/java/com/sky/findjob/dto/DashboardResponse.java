package com.sky.findjob.dto;

import java.time.LocalDateTime;
import java.util.List;

public record DashboardResponse(
        Metrics metrics,
        List<TrendPoint> trend,
        List<FunnelItem> funnel,
        List<StatusCount> statusDistribution,
        List<InterviewResponse> upcomingInterviews,
        List<ApplicationResponse> recentApplications,
        List<TaskItem> tasks
) {
    public record Metrics(
            MetricValue total,
            MetricValue active,
            MetricValue interviews,
            MetricValue offers,
            MetricValue responseRate,
            Weekly weekly
    ) {
    }

    public record Weekly(long applied, int target) {
    }

    public record FunnelItem(String key, String label, long value, double rate) {
    }

    public record TaskItem(String id, String title, String meta, String kind, LocalDateTime dueAt) {
    }
}
