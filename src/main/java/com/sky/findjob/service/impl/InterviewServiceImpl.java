package com.sky.findjob.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sky.findjob.common.BusinessException;
import com.sky.findjob.common.UserContext;
import com.sky.findjob.dto.InterviewRequest;
import com.sky.findjob.dto.InterviewResponse;
import com.sky.findjob.dto.InterviewStatusRequest;
import com.sky.findjob.dto.ListResponse;
import com.sky.findjob.entity.Application;
import com.sky.findjob.entity.Interview;
import com.sky.findjob.enums.ApplicationStatus;
import com.sky.findjob.enums.InterviewStatus;
import com.sky.findjob.mapper.ApplicationMapper;
import com.sky.findjob.mapper.InterviewMapper;
import com.sky.findjob.service.IInterviewService;
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

@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements IInterviewService {

    private static final String ALL = "ALL";
    private static final int DEFAULT_DURATION = 45;
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    private final InterviewMapper interviewMapper;
    private final ApplicationMapper applicationMapper;

    @Override
    @Transactional(readOnly = true)
    public ListResponse<InterviewResponse> list(LocalDate from, LocalDate to, String status) {
        Long userId = UserContext.require();
        if (from != null && to != null && from.isAfter(to)) {
            throw new BusinessException("开始日期不能晚于结束日期", HttpStatus.BAD_REQUEST);
        }

        InterviewStatus statusFilter = parseStatus(status);

        LambdaQueryWrapper<Interview> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Interview::getUserId, userId);
        if (statusFilter != null) {
            wrapper.eq(Interview::getStatus, statusFilter);
        }
        if (from != null) {
            wrapper.ge(Interview::getScheduledAt, from.atStartOfDay());
        }
        if (to != null) {
            wrapper.lt(Interview::getScheduledAt, to.plusDays(1).atStartOfDay());
        }
        wrapper.orderByAsc(Interview::getScheduledAt).orderByDesc(Interview::getId);

        List<InterviewResponse> items = interviewMapper.selectList(wrapper).stream()
                .map(InterviewResponse::from)
                .toList();
        return new ListResponse<>(items, items.size());
    }

    @Override
    @Transactional
    public InterviewResponse create(InterviewRequest request) {
        Long userId = UserContext.require();

        Interview interview = new Interview();
        interview.setUserId(userId);
        applyRequest(interview, request, userId);

        LocalDateTime now = LocalDateTime.now(ZONE).withNano(0);
        interview.setCreatedAt(now);
        interview.setUpdatedAt(now);
        interviewMapper.insert(interview);

        return InterviewResponse.from(interview);
    }

    @Override
    @Transactional
    public InterviewResponse update(Long id, InterviewRequest request) {
        Long userId = UserContext.require();
        requireOwned(id, userId);

        Interview interview = new Interview();
        applyRequest(interview, request, userId);

        interviewMapper.update(null, Wrappers.<Interview>lambdaUpdate()
                .eq(Interview::getId, id)
                .eq(Interview::getUserId, userId)
                .set(Interview::getApplicationId, interview.getApplicationId())
                .set(Interview::getCompany, interview.getCompany())
                .set(Interview::getPosition, interview.getPosition())
                .set(Interview::getRound, interview.getRound())
                .set(Interview::getType, interview.getType())
                .set(Interview::getScheduledAt, interview.getScheduledAt())
                .set(Interview::getDurationMinutes, interview.getDurationMinutes())
                .set(Interview::getLocation, interview.getLocation())
                .set(Interview::getLink, interview.getLink())
                .set(Interview::getInterviewer, interview.getInterviewer())
                .set(Interview::getStatus, interview.getStatus())
                .set(Interview::getNotes, interview.getNotes())
                .set(Interview::getUpdatedAt, LocalDateTime.now(ZONE).withNano(0)));

        return InterviewResponse.from(requireOwned(id, userId));
    }

    @Override
    @Transactional
    public InterviewResponse updateStatus(Long id, InterviewStatusRequest request) {
        Long userId = UserContext.require();
        requireOwned(id, userId);

        interviewMapper.update(null, Wrappers.<Interview>lambdaUpdate()
                .eq(Interview::getId, id)
                .eq(Interview::getUserId, userId)
                .set(Interview::getStatus, request.status())
                .set(Interview::getUpdatedAt, LocalDateTime.now(ZONE).withNano(0)));

        return InterviewResponse.from(requireOwned(id, userId));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Long userId = UserContext.require();
        requireOwned(id, userId);
        interviewMapper.deleteById(id);
    }

    private Interview requireOwned(Long id, Long userId) {
        Interview interview = interviewMapper.selectOne(Wrappers.<Interview>lambdaQuery()
                .eq(Interview::getId, id)
                .eq(Interview::getUserId, userId));
        if (interview == null) {
            throw new BusinessException("面试不存在", HttpStatus.NOT_FOUND);
        }
        return interview;
    }

    private void applyRequest(Interview interview, InterviewRequest request, Long userId) {
        Application application = null;
        if (StringUtils.hasText(request.applicationId())) {
            application = requireOwnedApplication(request.applicationId().trim(), userId);
        }

        if (application != null) {
            // 关联投递时自动带出公司 / 岗位，并把投递推进到面试中
            interview.setApplicationId(application.getId());
            interview.setCompany(application.getCompany());
            interview.setPosition(application.getPosition());
            if (application.getStatus() != ApplicationStatus.OFFER
                    && application.getStatus() != ApplicationStatus.REJECTED
                    && application.getStatus() != ApplicationStatus.INTERVIEW) {
                applicationMapper.update(null, Wrappers.<Application>lambdaUpdate()
                        .eq(Application::getId, application.getId())
                        .eq(Application::getUserId, userId)
                        .set(Application::getStatus, ApplicationStatus.INTERVIEW)
                        .set(Application::getUpdatedAt, LocalDateTime.now(ZONE).withNano(0)));
            }
        } else {
            if (!StringUtils.hasText(request.company())) {
                throw new BusinessException("公司名称不能为空", HttpStatus.BAD_REQUEST);
            }
            interview.setApplicationId(null);
            interview.setCompany(request.company().trim());
            interview.setPosition(trimToEmpty(request.position()));
        }

        interview.setRound(request.round().trim());
        interview.setType(request.type());
        interview.setScheduledAt(request.scheduledAt());
        interview.setDurationMinutes(request.durationMinutes() == null
                ? DEFAULT_DURATION : Math.max(request.durationMinutes(), 1));
        interview.setLocation(trimToEmpty(request.location()));
        interview.setLink(trimToEmpty(request.link()));
        interview.setInterviewer(trimToEmpty(request.interviewer()));
        interview.setStatus(request.status() == null ? InterviewStatus.UPCOMING : request.status());
        interview.setNotes(trimToEmpty(request.notes()));
    }

    private Application requireOwnedApplication(String rawId, Long userId) {
        String digits = rawId.replaceFirst("(?i)^app-", "");
        Long applicationId;
        try {
            applicationId = Long.valueOf(digits);
        } catch (NumberFormatException e) {
            throw new BusinessException("投递不存在", HttpStatus.NOT_FOUND);
        }

        Application application = applicationMapper.selectOne(Wrappers.<Application>lambdaQuery()
                .eq(Application::getId, applicationId)
                .eq(Application::getUserId, userId));
        if (application == null) {
            throw new BusinessException("投递不存在", HttpStatus.NOT_FOUND);
        }
        return application;
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private InterviewStatus parseStatus(String status) {
        if (!StringUtils.hasText(status) || ALL.equalsIgnoreCase(status.trim())) {
            return null;
        }
        try {
            return InterviewStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BusinessException("无效的面试状态：" + status, HttpStatus.BAD_REQUEST);
        }
    }
}
