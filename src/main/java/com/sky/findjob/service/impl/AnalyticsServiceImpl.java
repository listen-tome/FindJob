package com.sky.findjob.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sky.findjob.common.BusinessException;
import com.sky.findjob.common.UserContext;
import com.sky.findjob.dto.AnalyticsResponse;
import com.sky.findjob.dto.MetricValue;
import com.sky.findjob.dto.StatusCount;
import com.sky.findjob.dto.TrendPoint;
import com.sky.findjob.entity.Application;
import com.sky.findjob.entity.Interview;
import com.sky.findjob.enums.AnalyticsRange;
import com.sky.findjob.enums.ApplicationChannel;
import com.sky.findjob.enums.ApplicationStatus;
import com.sky.findjob.mapper.ApplicationMapper;
import com.sky.findjob.mapper.InterviewMapper;
import com.sky.findjob.service.IAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements IAnalyticsService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final Set<ApplicationStatus> RESPONDED_STATUSES = Set.of(
            ApplicationStatus.SCREENING, ApplicationStatus.INTERVIEW,
            ApplicationStatus.OFFER, ApplicationStatus.REJECTED);
    private static final Set<ApplicationStatus> INTERVIEW_STATUSES = Set.of(
            ApplicationStatus.INTERVIEW, ApplicationStatus.OFFER);

    private final ApplicationMapper applicationMapper;
    private final InterviewMapper interviewMapper;

    @Override
    @Transactional(readOnly = true)
    public AnalyticsResponse getAnalytics(String range) {
        Long userId = UserContext.require();
        AnalyticsRange analyticsRange = parseRange(range);

        LocalDate today = LocalDate.now(ZONE);
        LocalDate start = today.minusDays(analyticsRange.getDays() - 1L);
        LocalDate previousStart = start.minusDays(analyticsRange.getDays());
        LocalDate previousEnd = start.minusDays(1);

        List<Application> allApplications = applicationMapper.selectList(
                Wrappers.<Application>lambdaQuery().eq(Application::getUserId, userId));
        List<Interview> allInterviews = interviewMapper.selectList(
                Wrappers.<Interview>lambdaQuery().eq(Interview::getUserId, userId));

        List<Application> current = inPeriod(allApplications, start, today);
        List<Application> previous = inPeriod(allApplications, previousStart, previousEnd);

        MetricValue applications = new MetricValue(current.size(), current.size() - previous.size());

        double responseRate = rate(current, RESPONDED_STATUSES);
        double previousResponseRate = rate(previous, RESPONDED_STATUSES);
        MetricValue responseRateMetric = new MetricValue(responseRate,
                round1(responseRate - previousResponseRate));

        double interviewRate = rate(current, INTERVIEW_STATUSES);
        double previousInterviewRate = rate(previous, INTERVIEW_STATUSES);
        MetricValue interviewRateMetric = new MetricValue(interviewRate,
                round1(interviewRate - previousInterviewRate));

        double offerRate = rate(current, Set.of(ApplicationStatus.OFFER));
        double previousOfferRate = rate(previous, Set.of(ApplicationStatus.OFFER));
        MetricValue offerRateMetric = new MetricValue(offerRate,
                round1(offerRate - previousOfferRate));

        double avgResponseDays = averageResponseDays(current);
        double previousAvgResponseDays = averageResponseDays(previous);
        MetricValue avgResponseDaysMetric = new MetricValue(avgResponseDays,
                round1(avgResponseDays - previousAvgResponseDays));

        AnalyticsResponse.Summary summary = new AnalyticsResponse.Summary(
                applications, responseRateMetric, interviewRateMetric,
                offerRateMetric, avgResponseDaysMetric);

        List<TrendPoint> trend = switch (analyticsRange) {
            case LAST_30_DAYS -> buildDailyTrend(current, allInterviews, start, today);
            case LAST_90_DAYS -> buildWeeklyTrend(current, allInterviews, start, today);
            case LAST_365_DAYS -> buildMonthlyTrend(current, allInterviews, start, today);
        };

        List<AnalyticsResponse.ChannelStat> channels = buildChannels(current, allInterviews);
        List<StatusCount> statusDistribution = buildStatusDistribution(current);
        List<AnalyticsResponse.WeekdayStat> weekdays = buildWeekdays(current);

        return new AnalyticsResponse(analyticsRange.getCode(), summary, trend,
                channels, statusDistribution, weekdays);
    }

    private AnalyticsRange parseRange(String range) {
        if (range == null || range.isBlank()) {
            return AnalyticsRange.LAST_30_DAYS;
        }
        String value = range.trim().toLowerCase(Locale.ROOT);
        for (AnalyticsRange item : AnalyticsRange.values()) {
            if (item.getCode().equals(value)) {
                return item;
            }
        }
        throw new BusinessException("无效的分析区间：" + range, HttpStatus.BAD_REQUEST);
    }

    private List<Application> inPeriod(List<Application> applications, LocalDate from, LocalDate to) {
        return applications.stream()
                .filter(a -> a.getStatus() != ApplicationStatus.WISHLIST)
                .filter(a -> a.getAppliedAt() != null)
                .filter(a -> !a.getAppliedAt().isBefore(from) && !a.getAppliedAt().isAfter(to))
                .toList();
    }

    private double rate(List<Application> applications, Set<ApplicationStatus> statuses) {
        if (applications.isEmpty()) {
            return 0;
        }
        long matched = applications.stream().filter(a -> statuses.contains(a.getStatus())).count();
        return round1(matched * 100.0 / applications.size());
    }

    private double averageResponseDays(List<Application> applications) {
        List<Long> days = applications.stream()
                .filter(a -> RESPONDED_STATUSES.contains(a.getStatus()))
                .filter(a -> a.getAppliedAt() != null && a.getUpdatedAt() != null)
                .map(a -> Math.max(0, ChronoUnit.DAYS.between(a.getAppliedAt(), a.getUpdatedAt().toLocalDate())))
                .toList();
        if (days.isEmpty()) {
            return 0;
        }
        return round1(days.stream().mapToLong(Long::longValue).average().orElse(0));
    }

    private List<TrendPoint> buildDailyTrend(List<Application> applications,
                                             List<Interview> interviews,
                                             LocalDate start,
                                             LocalDate end) {
        Map<LocalDate, Long> applicationCounts = countApplicationsByDate(applications);
        Map<LocalDate, Long> interviewCounts = countInterviewsByDate(interviews);
        List<TrendPoint> trend = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            trend.add(new TrendPoint(
                    date.toString(),
                    date.getMonthValue() + "/" + date.getDayOfMonth(),
                    applicationCounts.getOrDefault(date, 0L),
                    interviewCounts.getOrDefault(date, 0L)));
        }
        return trend;
    }

    private List<TrendPoint> buildWeeklyTrend(List<Application> applications,
                                              List<Interview> interviews,
                                              LocalDate start,
                                              LocalDate end) {
        Map<LocalDate, Long> applicationCounts = countApplicationsByDate(applications);
        Map<LocalDate, Long> interviewCounts = countInterviewsByDate(interviews);
        List<TrendPoint> trend = new ArrayList<>();

        LocalDate weekStart = start.with(DayOfWeek.MONDAY);
        while (!weekStart.isAfter(end)) {
            LocalDate bucketStart = weekStart.isBefore(start) ? start : weekStart;
            LocalDate bucketEnd = weekStart.plusDays(6).isAfter(end) ? end : weekStart.plusDays(6);

            long applicationCount = 0;
            long interviewCount = 0;
            for (LocalDate date = bucketStart; !date.isAfter(bucketEnd); date = date.plusDays(1)) {
                applicationCount += applicationCounts.getOrDefault(date, 0L);
                interviewCount += interviewCounts.getOrDefault(date, 0L);
            }
            trend.add(new TrendPoint(bucketStart.toString(),
                    bucketStart.getMonthValue() + "/" + bucketStart.getDayOfMonth(),
                    applicationCount, interviewCount));
            weekStart = weekStart.plusWeeks(1);
        }
        return trend;
    }

    private List<TrendPoint> buildMonthlyTrend(List<Application> applications,
                                               List<Interview> interviews,
                                               LocalDate start,
                                               LocalDate end) {
        Map<LocalDate, Long> applicationCounts = countApplicationsByDate(applications);
        Map<LocalDate, Long> interviewCounts = countInterviewsByDate(interviews);
        List<TrendPoint> trend = new ArrayList<>();

        YearMonth month = YearMonth.from(start);
        YearMonth lastMonth = YearMonth.from(end);
        while (!month.isAfter(lastMonth)) {
            LocalDate bucketStart = month.atDay(1).isBefore(start) ? start : month.atDay(1);
            LocalDate bucketEnd = month.atEndOfMonth().isAfter(end) ? end : month.atEndOfMonth();

            long applicationCount = 0;
            long interviewCount = 0;
            for (LocalDate date = bucketStart; !date.isAfter(bucketEnd); date = date.plusDays(1)) {
                applicationCount += applicationCounts.getOrDefault(date, 0L);
                interviewCount += interviewCounts.getOrDefault(date, 0L);
            }
            trend.add(new TrendPoint(bucketStart.toString(),
                    month.getYear() + "/" + month.getMonthValue(),
                    applicationCount, interviewCount));
            month = month.plusMonths(1);
        }
        return trend;
    }

    private Map<LocalDate, Long> countApplicationsByDate(List<Application> applications) {
        Map<LocalDate, Long> counts = new HashMap<>();
        for (Application application : applications) {
            if (application.getAppliedAt() != null) {
                counts.merge(application.getAppliedAt(), 1L, Long::sum);
            }
        }
        return counts;
    }

    private Map<LocalDate, Long> countInterviewsByDate(List<Interview> interviews) {
        Map<LocalDate, Long> counts = new HashMap<>();
        for (Interview interview : interviews) {
            if (interview.getScheduledAt() != null) {
                counts.merge(interview.getScheduledAt().toLocalDate(), 1L, Long::sum);
            }
        }
        return counts;
    }

    private List<AnalyticsResponse.ChannelStat> buildChannels(List<Application> applications,
                                                              List<Interview> interviews) {
        Map<Long, ApplicationChannel> channelByApplicationId = new HashMap<>();
        for (Application application : applications) {
            channelByApplicationId.put(application.getId(), application.getChannel());
        }

        Map<ApplicationChannel, Long> interviewsByChannel = new HashMap<>();
        Set<Long> applicationIds = new HashSet<>(channelByApplicationId.keySet());
        for (Interview interview : interviews) {
            if (interview.getApplicationId() != null
                    && applicationIds.contains(interview.getApplicationId())) {
                ApplicationChannel channel = channelByApplicationId.get(interview.getApplicationId());
                interviewsByChannel.merge(channel, 1L, Long::sum);
            }
        }

        List<AnalyticsResponse.ChannelStat> channels = new ArrayList<>();
        for (ApplicationChannel channel : ApplicationChannel.values()) {
            List<Application> channelApplications = applications.stream()
                    .filter(a -> a.getChannel() == channel)
                    .toList();
            if (channelApplications.isEmpty()) {
                continue;
            }
            long interviewCount = interviewsByChannel.getOrDefault(channel, 0L);
            long offerCount = channelApplications.stream()
                    .filter(a -> a.getStatus() == ApplicationStatus.OFFER)
                    .count();
            long responded = channelApplications.stream()
                    .filter(a -> RESPONDED_STATUSES.contains(a.getStatus()))
                    .count();
            channels.add(new AnalyticsResponse.ChannelStat(
                    channel,
                    channelApplications.size(),
                    interviewCount,
                    offerCount,
                    round1(responded * 100.0 / channelApplications.size())));
        }
        return channels;
    }

    private List<StatusCount> buildStatusDistribution(List<Application> applications) {
        List<StatusCount> distribution = new ArrayList<>();
        for (ApplicationStatus status : ApplicationStatus.values()) {
            long count = applications.stream().filter(a -> a.getStatus() == status).count();
            if (count > 0) {
                distribution.add(new StatusCount(status, count));
            }
        }
        return distribution;
    }

    private List<AnalyticsResponse.WeekdayStat> buildWeekdays(List<Application> applications) {
        String[] labels = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        long[] counts = new long[7];
        for (Application application : applications) {
            counts[application.getAppliedAt().getDayOfWeek().getValue() - 1]++;
        }
        List<AnalyticsResponse.WeekdayStat> weekdays = new ArrayList<>(7);
        for (int i = 0; i < 7; i++) {
            weekdays.add(new AnalyticsResponse.WeekdayStat(labels[i], counts[i]));
        }
        return weekdays;
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
