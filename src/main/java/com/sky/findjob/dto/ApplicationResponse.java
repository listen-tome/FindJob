package com.sky.findjob.dto;

import com.sky.findjob.entity.Application;
import com.sky.findjob.enums.ApplicationChannel;
import com.sky.findjob.enums.ApplicationPriority;
import com.sky.findjob.enums.ApplicationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 返回给前端的投递对象，id / resumeId 统一转为字符串。
 */
public record ApplicationResponse(
        String id,
        String company,
        String position,
        String location,
        String salary,
        ApplicationChannel channel,
        ApplicationStatus status,
        ApplicationPriority priority,
        LocalDate appliedAt,
        LocalDate nextActionAt,
        List<String> tags,
        String notes,
        String resumeId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ApplicationResponse from(Application application) {
        return new ApplicationResponse(
                String.valueOf(application.getId()),
                application.getCompany(),
                application.getPosition(),
                application.getLocation(),
                application.getSalary(),
                application.getChannel(),
                application.getStatus(),
                application.getPriority(),
                application.getAppliedAt(),
                application.getNextActionAt(),
                application.getTags() == null ? List.of() : application.getTags(),
                application.getNotes(),
                application.getResumeId() == null ? null : String.valueOf(application.getResumeId()),
                application.getCreatedAt(),
                application.getUpdatedAt()
        );
    }
}
