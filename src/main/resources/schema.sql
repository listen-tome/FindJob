-- ============================================================
-- FindJob 表结构（可重复执行）
-- 启动时由 spring.sql.init.mode=always 自动执行
-- 全部使用 CREATE TABLE IF NOT EXISTS，不会清空已有数据
-- ============================================================

-- ------------------------------------------------------------
-- 用户
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `users` (
    `id`              bigint       NOT NULL AUTO_INCREMENT,
    `name`            varchar(50)  NOT NULL,
    `email`           varchar(120) NOT NULL,
    `password_hash`   varchar(255) NOT NULL,
    `school`          varchar(80)  NOT NULL DEFAULT '',
    `major`           varchar(80)  NOT NULL DEFAULT '',
    `graduation_year` smallint     NOT NULL DEFAULT 2027,
    `target_role`     varchar(80)  NOT NULL DEFAULT '',
    `location`        varchar(80)  NOT NULL DEFAULT '',
    `bio`             varchar(300) NOT NULL DEFAULT '',
    `weekly_goal`     tinyint      NOT NULL DEFAULT 8,
    `created_at`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_users_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 投递记录
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `applications` (
    `id`             bigint        NOT NULL AUTO_INCREMENT,
    `user_id`        bigint        NOT NULL,
    `resume_id`      bigint        DEFAULT NULL,
    `company`        varchar(40)   NOT NULL,
    `position`       varchar(60)   NOT NULL,
    `location`       varchar(80)   NOT NULL DEFAULT '',
    `salary`         varchar(40)   NOT NULL DEFAULT '',
    `channel`        varchar(20)   NOT NULL DEFAULT 'OTHER',
    `status`         varchar(20)   NOT NULL DEFAULT 'WISHLIST',
    `priority`       varchar(10)   NOT NULL DEFAULT 'MEDIUM',
    `applied_at`     date          DEFAULT NULL,
    `next_action_at` date          DEFAULT NULL,
    `tags`           json          DEFAULT NULL,
    `notes`          varchar(1000) NOT NULL DEFAULT '',
    `created_at`     datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`     datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_applications_user_status` (`user_id`, `status`),
    KEY `idx_applications_user_updated` (`user_id`, `updated_at`),
    KEY `idx_applications_user_applied` (`user_id`, `applied_at`),
    KEY `idx_applications_next_action` (`user_id`, `next_action_at`),
    KEY `idx_applications_resume` (`resume_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 面试日程
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `interviews` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint NOT NULL,
    `application_id` bigint DEFAULT NULL,
    `company` varchar(40) NOT NULL,
    `position` varchar(60) NOT NULL DEFAULT '',
    `round` varchar(50) NOT NULL,
    `type` varchar(20) NOT NULL,
    `scheduled_at` datetime NOT NULL,
    `duration_minutes` int NOT NULL DEFAULT 45,
    `location` varchar(100) NOT NULL DEFAULT '',
    `link` varchar(500) NOT NULL DEFAULT '',
    `interviewer` varchar(100) NOT NULL DEFAULT '',
    `status` varchar(20) NOT NULL,
    `notes` text,
    `created_at` datetime NOT NULL,
    `updated_at` datetime NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_interview_user_time` (`user_id`, `scheduled_at`),
    KEY `idx_interview_user_status` (`user_id`, `status`),
    KEY `idx_interview_application` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 简历
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `resumes` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint NOT NULL,
    `name` varchar(100) NOT NULL,
    `target_role` varchar(100) NOT NULL,
    `version` int NOT NULL DEFAULT 1,
    `file_name` varchar(255) NOT NULL,
    `file_size` bigint NOT NULL DEFAULT 0,
    `file_type` varchar(100) NOT NULL DEFAULT '',
    `file_path` varchar(500) NOT NULL,
    `is_default` tinyint(1) NOT NULL DEFAULT 0,
    `created_at` datetime NOT NULL,
    `updated_at` datetime NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_resume_user_updated` (`user_id`, `updated_at` DESC),
    KEY `idx_resume_user_role` (`user_id`, `target_role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
