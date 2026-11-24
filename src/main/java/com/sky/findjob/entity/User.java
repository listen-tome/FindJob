package com.sky.findjob.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;
/**
 * 用户实体类
 */
@Data
@TableName("users")
public class User {

    /**
     * 主键，数据库自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 姓名
     */
    private String name;

    /**
     * 登录邮箱，唯一
     */
    private String email;

    /**
     * BCrypt 加密后的密码
     * 不返回给前端
     */
    @JsonIgnore
    private String passwordHash;

    /**
     * 学校
     */
    private String school;

    /**
     * 专业
     */
    private String major;

    /**
     * 毕业年份
     */
    private Integer graduationYear;

    /**
     * 目标岗位
     */
    private String targetRole;

    /**
     * 期望城市
     */
    private String location;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 每周投递目标
     */
    private Integer weeklyGoal;

    /**
     * 创建时间，数据库默认值
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间，数据库自动更新
     */
    private LocalDateTime updatedAt;
}