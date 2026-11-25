package com.sky.findjob.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 简历实体类，对应 resumes 表。
 */
@Data
@TableName("resumes")
public class Resume {

    @TableId(type = IdType.AUTO)
    private Long id;

    @JsonIgnore
    private Long userId;

    private String name;

    private String targetRole;

    private Integer version;

    private String fileName;

    private Long fileSize;

    private String fileType;

    /**
     * 磁盘上的相对文件名（UUID + 扩展名），不存完整 URL
     */
    private String filePath;

    @TableField("is_default")
    private Boolean isDefault;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
