package com.sky.findjob.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sky.findjob.common.UserContext;
import com.sky.findjob.dto.ApplicationResponse;
import com.sky.findjob.dto.DashboardResponse;
import com.sky.findjob.dto.InterviewResponse;
import com.sky.findjob.dto.MetricValue;
import com.sky.findjob.dto.StatusCount;
import com.sky.findjob.dto.TrendPoint;
import com.sky.findjob.entity.Application;
import com.sky.findjob.entity.Interview;
import com.sky.findjob.entity.User;
import com.sky.findjob.enums.ApplicationStatus;
import com.sky.findjob.enums.InterviewStatus;
import com.sky.findjob.mapper.ApplicationMapper;
import com.sky.findjob.mapper.InterviewMapper;
import com.sky.findjob.mapper.UserMapper;
import com.sky.findjob.service.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements IDashboardService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final Set<ApplicationStatus> ACTIVE_STATUSES = Set.of(
            ApplicationStatus.APPLIED, ApplicationStatus.SCREENING, ApplicationStatus.INTERVIEW);
    private static final Set<ApplicationStatus> RESPONDED_STATUSES = Set.of(
            ApplicationStatus.SCREENING, ApplicationStatus.INTERVIEW,
            ApplicationStatus.OFFER, ApplicationStatus.REJECTED);

    private final ApplicationMapper applicationMapper;
    private final InterviewMapper interviewMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        Long userId = UserContext.require();
        LocalDateTime now = LocalDateTime.now(ZONE);
        LocalDate today = now.toLocalDate();

        List<Application> applications = applicationMapper.selectList(
                Wrappers.<Application>lambdaQuery().eq(Application::getUserId, userId));
        List<Interview> interviews = interviewMapper.selectList(
                Wrappers.<Interview>lambdaQuery().eq(Interview::getUserId, userId));

        List<Application> submitted = applications.stream()
                .filter(a -> a.getStatus() != ApplicationStatus.WISHLIST)
                .toList();

        long total = submitted.size();
        long totalDelta = submitted.stream()
                .filter(a -> inLastDays(a.getAppliedAt(), today, 7))
                .count();
        long active = submitted.stream().filter(a -> ACTIVE_STATUSES.contains(a.getStatus())).count();
        long activeDelta = submitted.stream()
                .filter(a -> ACTIVE_STATUSES.contains(a.getStatus()))
                .filter(a -> a.getUpdatedAt() != null && !a.getUpdatedAt().isBefore(now.minusDays(7)))
                .count();

        List<Interview> upcoming = interviews.stream()
                .filter(i -> i.getStatus() == InterviewStatus.UPCOMING)
                .filter(i -> i.getScheduledAt() != null && !i.getScheduledAt().isBefore(now))
                .sorted(Comparator.comparing(Interview::getScheduledAt))
                .toList();
        long interviewDelta = upcoming.stream()
                .filter(i -> i.getCreatedAt() != null && !i.getCreatedAt().isBefore(now.minusDays(7)))
                .count();

        List<Application> offers = submitted.stream()
                .filter(a -> a.getStatus() == ApplicationStatus.OFFER)
                .toList();
        long offerDelta = offers.stream()
                .filter(a -> a.getUpdatedAt() != null && !a.getUpdatedAt().isBefore(now.minusDays(7)))
                .count();

        double responseRate = percentage(
                submitted.stream().filter(a -> RESPONDED_STATUSES.contains(a.getStatus())).count(), total);
        double currentPeriodRate = responseRateForPeriod(applications, today.minusDays(6), today);
        double previousPeriodRate = responseRateForPeriod(applications, today.minusDays(13), today.minusDays(7));
        double responseDelta = round1(currentPeriodRate - previousPeriodRate);

        long weeklyApplied = submitted.stream()
                .filter(a -> a.getAppliedAt() != null)
                .filter(a -> !a.getAppliedAt().isBefore(today.with(DayOfWeek.MONDAY)))
                .filter(a -> !a.getAppliedAt().isAfter(today))
                .count();
        User user = userMapper.selectById(userId);
        int weeklyTarget = user == null || user.getWeeklyGoal() == null ? 8 : user.getWeeklyGoal();

        DashboardResponse.Metrics metrics = new DashboardResponse.Metrics(
                new MetricValue(total, totalDelta),
                new MetricValue(active, activeDelta),
                new MetricValue(upcoming.size(), interviewDelta),
                new MetricValue(offers.size(), offerDelta),
                new MetricValue(responseRate, responseDelta),
                new DashboardResponse.Weekly(weeklyApplied, weeklyTarget)
        );

        List<TrendPoint> trend = buildTrend(submitted, interviews, today);

        long screening = submitted.stream()
                .filter(a -> a.getStatus() == ApplicationStatus.SCREENING
                        || a.getStatus() == ApplicationStatus.INTERVIEW
                        || a.getStatus() == ApplicationStatus.OFFER
                        || a.getStatus() == ApplicationStatus.REJECTED)
                .count();
        long interviewCount = submitted.stream()
                .filter(a -> a.getStatus() == ApplicationStatus.INTERVIEW
                        || a.getStatus() == ApplicationStatus.OFFER)
                .count();
        List<DashboardResponse.FunnelItem> funnel = List.of(
                new DashboardResponse.FunnelItem("applied", "已投递", total, total == 0 ? 0 : 100),
                new DashboardResponse.FunnelItem("screening", "进入筛选", screening, percentage(screening, total)),
                new DashboardResponse.FunnelItem("interview", "进入面试", interviewCount, percentage(interviewCount, screening)),
                new DashboardResponse.FunnelItem("offer", "收获 Offer", offers.size(), percentage(offers.size(), interviewCount))
        );

        List<StatusCount> distribution = new ArrayList<>();
        for (ApplicationStatus status : ApplicationStatus.values()) {
            distribution.add(new StatusCount(status,
                    applications.stream().filter(a -> a.getStatus() == status).count()));
        }

        List<InterviewResponse> upcomingInterviews = upcoming.stream()
                .limit(3)
                .map(InterviewResponse::from)
                .toList();

        List<ApplicationResponse> recentApplications = applications.stream()
                .sorted(Comparator.comparing(Application::getUpdatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .map(ApplicationResponse::from)
                .toList();

        List<DashboardResponse.TaskItem> tasks = buildTasks(submitted, upcoming, now);

        return new DashboardResponse(metrics, trend, funnel, distribution,
                upcomingInterviews, recentApplications, tasks);
    }

    private List<TrendPoint> buildTrend(List<Application> applications,
                                        List<Interview> interviews,
                                        LocalDate today) {
        Map<LocalDate, Long> applicationCounts = new HashMap<>();
        for (Application application : applications) {
            if (application.getAppliedAt() != null) {
                applicationCounts.merge(application.getAppliedAt(), 1L, Long::sum);
            }
        }
        Map<LocalDate, Long> interviewCounts = new HashMap<>();
        for (Interview interview : interviews) {
            if (interview.getScheduledAt() != null) {
                interviewCounts.merge(interview.getScheduledAt().toLocalDate(), 1L, Long::sum);
            }
        }

        List<TrendPoint> trend = new ArrayList<>(14);
        for (int i = 13; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            trend.add(new TrendPoint(
                    date.toString(),
                    date.getMonthValue() + "/" + date.getDayOfMonth(),
                    applicationCounts.getOrDefault(date, 0L),
                    interviewCounts.getOrDefault(date, 0L)
            ));
        }
        return trend;
    }

    private List<DashboardResponse.TaskItem> buildTasks(List<Application> submitted,
                                                        List<Interview> upcoming,
                                                        LocalDateTime now) {
        List<DashboardResponse.TaskItem> tasks = new ArrayList<>(4);

        upcoming.stream().findFirst().ifPresent(interview ->
                tasks.add(new DashboardResponse.TaskItem(
                        "interview-" + interview.getId(),
                        "准备「" + interview.getCompany() + "」" + interview.getRound(),
                        dateTimeLabel(interview.getScheduledAt()) + " · " + interview.getType().getDescription() + "面",
                        "INTERVIEW",
                        interview.getScheduledAt())));

        submitted.stream()
                .filter(a -> a.getStatus() == ApplicationStatus.APPLIED
                        || a.getStatus() == ApplicationStatus.SCREENING)
                .filter(a -> a.getNextActionAt() != null)
                .sorted(Comparator.comparing(Application::getNextActionAt))
                .limit(Math.max(0, 4 - tasks.size()))
                .forEach(a -> tasks.add(new DashboardResponse.TaskItem(
                        "follow-up-" + a.getId(),
                        "跟进「" + a.getCompany() + "」的投递进度",
                        monthDay(a.getNextActionAt()) + " 前",
                        "FOLLOW_UP",
                        a.getNextActionAt().atStartOfDay())));

        if (tasks.size() < 4) {
            submitted.stream()
                    .filter(a -> a.getStatus() == ApplicationStatus.WISHLIST)
                    .filter(a -> a.getCreatedAt() != null)
                    .min(Comparator.comparing(Application::getCreatedAt))
                    .ifPresent(a -> tasks.add(new DashboardResponse.TaskItem(
                            "review-" + a.getId(),
                            "完善「" + a.getCompany() + "」的投递材料",
                            "尚未投递",
                            "REVIEW",
                            null)));
        }

        return tasks.stream().limit(4).toList();
    }

    private double responseRateForPeriod(List<Application> applications, LocalDate from, LocalDate to) {
        List<Application> period = applications.stream()
                .filter(a -> a.getStatus() != ApplicationStatus.WISHLIST)
                .filter(a -> a.getAppliedAt() != null)
                .filter(a -> !a.getAppliedAt().isBefore(from) && !a.getAppliedAt().isAfter(to))
                .toList();
        long responded = period.stream().filter(a -> RESPONDED_STATUSES.contains(a.getStatus())).count();
        return percentage(responded, period.size());
    }

    private boolean inLastDays(LocalDate date, LocalDate today, int days) {
        return date != null && !date.isBefore(today.minusDays(days - 1L)) && !date.isAfter(today);
    }

    private double percentage(long value, long total) {
        return total == 0 ? 0 : round1(value * 100.0 / total);
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private String dateTimeLabel(LocalDateTime dateTime) {
        return dateTime.getMonthValue() + "月" + dateTime.getDayOfMonth() + "日 "
                + String.format("%02d:%02d", dateTime.getHour(), dateTime.getMinute());
    }

    private String monthDay(LocalDate date) {
        return date.getMonthValue() + "月" + date.getDayOfMonth() + "日";
    }
}
