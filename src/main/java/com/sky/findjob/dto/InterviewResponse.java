package com.sky.findjob.dto;

import com.sky.findjob.entity.Interview;
import com.sky.findjob.enums.InterviewStatus;
import com.sky.findjob.enums.InterviewType;

import java.time.LocalDateTime;

public record InterviewResponse(
        String id,
        String applicationId,
        String company,
        String position,
        String round,
        InterviewType type,
        LocalDateTime scheduledAt,
        Integer durationMinutes,
        String location,
        String link,
        String interviewer,
        InterviewStatus status,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static InterviewResponse from(Interview interview) {
        return new InterviewResponse(
                String.valueOf(interview.getId()),
                interview.getApplicationId() == null ? null : String.valueOf(interview.getApplicationId()),
                interview.getCompany(),
                interview.getPosition(),
                interview.getRound(),
                interview.getType(),
                interview.getScheduledAt(),
                interview.getDurationMinutes(),
                interview.getLocation(),
                interview.getLink(),
                interview.getInterviewer(),
                interview.getStatus(),
                interview.getNotes(),
                interview.getCreatedAt(),
                interview.getUpdatedAt()
        );
    }
}
