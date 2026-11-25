package com.sky.findjob.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sky.findjob.enums.InterviewStatus;
import com.sky.findjob.enums.InterviewType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 面试日程实体类，对应 interviews 表。
 */
@Data
@TableName("interviews")
public class Interview {

    @TableId(type = IdType.AUTO)
    private Long id;

    @JsonIgnore
    private Long userId;

    private Long applicationId;

    private String company;

    private String position;

    private String round;

    private InterviewType type;

    private LocalDateTime scheduledAt;

    private Integer durationMinutes;

    private String location;

    private String link;

    private String interviewer;

    private InterviewStatus status;

    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
