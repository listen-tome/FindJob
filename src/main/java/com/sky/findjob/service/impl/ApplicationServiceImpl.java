package com.sky.findjob.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.findjob.common.BusinessException;
import com.sky.findjob.common.UserContext;
import com.sky.findjob.dto.ApplicationResponse;
import com.sky.findjob.dto.ApplicationSaveRequest;
import com.sky.findjob.dto.ApplicationStatusRequest;
import com.sky.findjob.dto.PageResponse;
import com.sky.findjob.entity.Application;
import com.sky.findjob.entity.Interview;
import com.sky.findjob.entity.Resume;
import com.sky.findjob.enums.ApplicationChannel;
import com.sky.findjob.enums.ApplicationStatus;
import com.sky.findjob.mapper.ApplicationMapper;
import com.sky.findjob.mapper.InterviewMapper;
import com.sky.findjob.mapper.ResumeMapper;
import com.sky.findjob.service.IApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements IApplicationService {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final String DEFAULT_SORT = "updatedAt,desc";
    private static final String ALL = "ALL";
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    private final ApplicationMapper applicationMapper;
    private final InterviewMapper interviewMapper;
    private final ResumeMapper resumeMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ApplicationResponse> page(String keyword,
                                                  String status,
                                                  String channel,
                                                  Integer page,
                                                  Integer size,
                                                  String sort) {
        Long userId = UserContext.require();

        int currentPage = (page == null || page < 1) ? DEFAULT_PAGE : page;
        int pageSize = (size == null) ? DEFAULT_SIZE : Math.min(Math.max(size, 1), MAX_SIZE);

        ApplicationStatus statusFilter = parseStatus(status);
        ApplicationChannel channelFilter = parseChannel(channel);

        LambdaQueryWrapper<Application> wrapper = Wrappers.lambdaQuery();
        // 所有查询都必须限定当前登录用户
        wrapper.eq(Application::getUserId, userId);

        if (statusFilter != null) {
            wrapper.eq(Application::getStatus, statusFilter);
        }
        if (channelFilter != null) {
            wrapper.eq(Application::getChannel, channelFilter);
        }

        String kw = keyword == null ? null : keyword.trim();
        if (StringUtils.hasText(kw)) {
            wrapper.and(w -> w.like(Application::getCompany, kw)
                    .or().like(Application::getPosition, kw)
                    .or().like(Application::getLocation, kw)
                    .or().like(Application::getNotes, kw)
                    // tags 是 JSON 数组，用 JSON_SEARCH 做模糊匹配，参数化避免 SQL 注入
                    .or().apply("JSON_SEARCH(tags, 'one', CONCAT('%', {0}, '%')) IS NOT NULL", kw));
        }

        applySort(wrapper, sort);

        Page<Application> result = applicationMapper.selectPage(
                new Page<>(currentPage, pageSize), wrapper);

        List<ApplicationResponse> items = result.getRecords().stream()
                .map(ApplicationResponse::from)
                .toList();

        return new PageResponse<>(items, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationResponse get(Long id) {
        return ApplicationResponse.from(requireOwned(id, UserContext.require()));
    }

    @Override
    @Transactional
    public ApplicationResponse create(ApplicationSaveRequest request) {
        Long userId = UserContext.require();

        Application application = new Application();
        application.setUserId(userId);
        applyRequest(application, request, userId);

        LocalDateTime now = LocalDateTime.now(ZONE).withNano(0);
        application.setCreatedAt(now);
        application.setUpdatedAt(now);
        applicationMapper.insert(application);

        return ApplicationResponse.from(application);
    }

    @Override
    @Transactional
    public ApplicationResponse update(Long id, ApplicationSaveRequest request) {
        Long userId = UserContext.require();
        requireOwned(id, userId);

        Application update = new Application();
        applyRequest(update, request, userId);
        LocalDate appliedAt = update.getAppliedAt();
        LocalDate nextActionAt = update.getNextActionAt();
        Long resumeId = update.getResumeId();
        LocalDateTime now = LocalDateTime.now(ZONE).withNano(0);
        update.setUpdatedAt(now);
        update.setAppliedAt(null);
        update.setNextActionAt(null);
        update.setResumeId(null);

        // 可空字段放入 UpdateWrapper，确保能被显式置为 NULL
        applicationMapper.update(update, Wrappers.<Application>lambdaUpdate()
                .eq(Application::getId, id)
                .eq(Application::getUserId, userId)
                .set(Application::getAppliedAt, appliedAt)
                .set(Application::getNextActionAt, nextActionAt)
                .set(Application::getResumeId, resumeId));

        return ApplicationResponse.from(requireOwned(id, userId));
    }

    @Override
    @Transactional
    public ApplicationResponse updateStatus(Long id, ApplicationStatusRequest request) {
        Long userId = UserContext.require();
        Application application = requireOwned(id, userId);

        // 拖拽只更新 status / updatedAt；若首次进入已投递阶段则补 appliedAt
        var update = Wrappers.<Application>lambdaUpdate()
                .eq(Application::getId, application.getId())
                .eq(Application::getUserId, userId)
                .set(Application::getStatus, request.status())
                .set(Application::getUpdatedAt, LocalDateTime.now(ZONE).withNano(0));
        if (request.status() != ApplicationStatus.WISHLIST && application.getAppliedAt() == null) {
            update.set(Application::getAppliedAt, LocalDate.now(ZONE));
        }
        applicationMapper.update(null, update);

        return ApplicationResponse.from(requireOwned(id, userId));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Long userId = UserContext.require();
        requireOwned(id, userId);

        // 删除投递时级联删除关联面试
        interviewMapper.delete(Wrappers.<Interview>lambdaQuery()
                .eq(Interview::getUserId, userId)
                .eq(Interview::getApplicationId, id));
        applicationMapper.deleteById(id);
    }

    private Application requireOwned(Long id, Long userId) {
        Application application = applicationMapper.selectOne(Wrappers.<Application>lambdaQuery()
                .eq(Application::getId, id)
                .eq(Application::getUserId, userId));
        if (application == null) {
            throw new BusinessException("投递不存在", HttpStatus.NOT_FOUND);
        }
        return application;
    }

    private void applyRequest(Application application, ApplicationSaveRequest request, Long userId) {
        application.setCompany(request.company().trim());
        application.setPosition(request.position().trim());
        application.setLocation(trimToEmpty(request.location()));
        application.setSalary(trimToEmpty(request.salary()));
        application.setChannel(request.channel());
        application.setStatus(request.status());
        application.setPriority(request.priority());
        application.setNextActionAt(request.nextActionAt());
        application.setNotes(trimToEmpty(request.notes()));
        application.setTags(normalizeTags(request.tags()));
        application.setResumeId(validateResume(request.resumeId(), userId));

        LocalDate appliedAt = request.appliedAt();
        if (request.status() != ApplicationStatus.WISHLIST && appliedAt == null) {
            appliedAt = LocalDate.now(ZONE);
        }
        application.setAppliedAt(appliedAt);
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private List<String> normalizeTags(List<String> tags) {
        if (tags == null) {
            return List.of();
        }
        return tags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();
    }

    /**
     * resumeId 必须存在且属于当前用户，或为 null。
     * 兼容前端可能传来的 "1" / "res-1" 两种写法。
     */
    private Long validateResume(String rawResumeId, Long userId) {
        if (!StringUtils.hasText(rawResumeId)) {
            return null;
        }
        String digits = rawResumeId.trim().replaceFirst("(?i)^res-", "");
        Long resumeId;
        try {
            resumeId = Long.valueOf(digits);
        } catch (NumberFormatException e) {
            throw new BusinessException("简历不存在", HttpStatus.NOT_FOUND);
        }

        Resume resume = resumeMapper.selectOne(Wrappers.<Resume>lambdaQuery()
                .eq(Resume::getId, resumeId)
                .eq(Resume::getUserId, userId));
        if (resume == null) {
            throw new BusinessException("简历不存在", HttpStatus.NOT_FOUND);
        }
        return resumeId;
    }

    private ApplicationStatus parseStatus(String status) {
        if (!StringUtils.hasText(status) || ALL.equalsIgnoreCase(status.trim())) {
            return null;
        }
        try {
            return ApplicationStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BusinessException("无效的投递状态：" + status, HttpStatus.BAD_REQUEST);
        }
    }

    private ApplicationChannel parseChannel(String channel) {
        if (!StringUtils.hasText(channel) || ALL.equalsIgnoreCase(channel.trim())) {
            return null;
        }
        try {
            return ApplicationChannel.valueOf(channel.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BusinessException("无效的投递渠道：" + channel, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 排序字段白名单，只允许以下字段 + 方向，避免拼接 SQL。
     */
    private void applySort(LambdaQueryWrapper<Application> wrapper, String sort) {
        String value = StringUtils.hasText(sort) ? sort.trim() : DEFAULT_SORT;
        String[] parts = value.split(",");
        if (parts.length != 2) {
            throw new BusinessException("排序参数格式应为「字段,asc|desc」", HttpStatus.BAD_REQUEST);
        }

        String field = parts[0].trim();
        String direction = parts[1].trim().toLowerCase(Locale.ROOT);
        if (!"asc".equals(direction) && !"desc".equals(direction)) {
            throw new BusinessException("排序方向只支持 asc 或 desc", HttpStatus.BAD_REQUEST);
        }
        boolean asc = "asc".equals(direction);

        switch (field) {
            case "updatedAt" -> wrapper.orderBy(true, asc, Application::getUpdatedAt);
            case "appliedAt" -> wrapper.orderBy(true, asc, Application::getAppliedAt);
            case "company" -> wrapper.orderBy(true, asc, Application::getCompany);
            default -> throw new BusinessException("不支持的排序字段：" + field, HttpStatus.BAD_REQUEST);
        }

        // 兜底排序，保证分页结果稳定
        wrapper.orderByDesc(Application::getId);
    }
}
