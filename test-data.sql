-- ============================================================
-- FindJob 投递管理 · 测试数据
-- 适用表：applications
-- 执行前：把 @uid 改成当前登录用户的 id，
--         或保留下面的子查询（默认取 id 最小的用户）。
-- ============================================================

SET @uid := (SELECT id FROM users ORDER BY id LIMIT 1);

INSERT INTO applications
    (user_id, company, position, location, salary, channel, status, priority,
     applied_at, next_action_at, tags, notes, resume_id, created_at, updated_at)
VALUES
    -- 1. 面试中：标签 + 备注 + 有下一待办
    (@uid, '字节跳动', '前端开发实习生', '北京 · 海淀', '300–400/天', 'BOSS', 'INTERVIEW', 'HIGH',
     '2026-11-07', '2026-11-27', JSON_ARRAY('React', 'TypeScript', '大厂'),
     '一面已通过，重点准备性能优化和项目难点。', NULL,
     '2026-11-07 10:00:00', '2026-11-18 18:00:00'),

    -- 2. 筛选中：内推渠道
    (@uid, '腾讯', '后端开发实习生', '深圳 · 南山', '250–350/天', 'REFERRAL', 'SCREENING', 'MEDIUM',
     '2026-11-12', '2026-11-26', JSON_ARRAY('Java', 'Spring', 'MySQL'),
     '学长内推，HR 说本周内安排一面。', NULL,
     '2026-11-12 09:30:00', '2026-11-20 14:20:00'),

    -- 3. 已投递：官网渠道
    (@uid, '阿里巴巴', '数据开发实习生', '杭州 · 余杭', '200–300/天', 'OFFICIAL', 'APPLIED', 'MEDIUM',
     '2026-11-18', '2026-11-25', JSON_ARRAY('SQL', '数据仓库'),
     '官网投递，等待笔试通知。', NULL,
     '2026-11-18 20:10:00', '2026-11-18 20:10:00'),

    -- 4. 想投递：applied_at 允许为空
    (@uid, '美团', '产品经理实习生', '北京 · 朝阳', '180–250/天', 'BOSS', 'WISHLIST', 'HIGH',
     NULL, '2026-11-28', JSON_ARRAY('产品', '用户研究'),
     '岗位匹配度高，准备按 JD 改一版简历再投。', NULL,
     '2026-11-20 11:00:00', '2026-11-22 16:40:00'),

    -- 5. 面试中：领英渠道
    (@uid, '小红书', '前端开发实习生', '上海 · 黄浦', '250–350/天', 'LINKEDIN', 'INTERVIEW', 'HIGH',
     '2026-10-28', '2026-11-29', JSON_ARRAY('Vue', 'TypeScript', '内容社区'),
     '二面约在 11/29 下午，准备组件库和工程化相关项目。', NULL,
     '2026-10-28 15:00:00', '2026-11-21 10:30:00'),

    -- 6. 已获 Offer：校招渠道
    (@uid, '华为', '软件开发实习生', '南京 · 雨花台', '200–300/天', 'CAMPUS', 'OFFER', 'HIGH',
     '2026-10-15', NULL, JSON_ARRAY('C++', '操作系统'),
     '已收到实习 Offer，11/30 前确认是否接受。', NULL,
     '2026-10-15 09:00:00', '2026-11-19 17:00:00'),

    -- 7. 未通过：流程结束
    (@uid, '网易', '游戏客户端开发实习生', '杭州 · 滨江', '220–320/天', 'OFFICIAL', 'REJECTED', 'LOW',
     '2026-10-08', NULL, JSON_ARRAY('Unity', 'C#'),
     '笔试未通过，已复盘算法题。', NULL,
     '2026-10-08 13:20:00', '2026-10-30 18:00:00'),

    -- 8. 已投递：其他渠道
    (@uid, '百度', '算法实习生', '北京 · 海淀', '300–500/天', 'OTHER', 'APPLIED', 'LOW',
     '2026-11-21', '2026-11-30', JSON_ARRAY('Python', '机器学习'),
     '官网投递，暂无回复。', NULL,
     '2026-11-21 21:15:00', '2026-11-21 21:15:00');

-- 查看插入结果
SELECT id, company, position, status, channel, priority, applied_at, next_action_at, tags
FROM applications
WHERE user_id = @uid
ORDER BY updated_at DESC;

-- 如需清理这批测试数据（按公司名匹配，执行前请确认）：
-- DELETE FROM applications
-- WHERE user_id = @uid
--   AND company IN ('字节跳动', '腾讯', '阿里巴巴', '美团', '小红书', '华为', '网易', '百度');
