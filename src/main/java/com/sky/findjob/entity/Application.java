package com.sky.findjob.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sky.findjob.enums.ApplicationChannel;
import com.sky.findjob.enums.ApplicationPriority;
import com.sky.findjob.enums.ApplicationStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 投递实体类，对应 applications 表。
 */
@Data
@TableName(value = "applications", autoResultMap = true)
public class Application {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户，业务上永远从登录态获取，不接收前端传值
     */
    @JsonIgnore
    private Long userId;

    private String company;

    private String position;

    private String location;

    private String salary;

    private ApplicationChannel channel;

    private ApplicationStatus status;

    private ApplicationPriority priority;

    private LocalDate appliedAt;

    private LocalDate nextActionAt;

    /**
     * 数据库为 JSON 数组，读写时由 JacksonTypeHandler 转换
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    private String notes;

    private Long resumeId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
