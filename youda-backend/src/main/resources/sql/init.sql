-- 优答·中小学答疑平台初始化脚本
-- 说明：
-- 1. 本脚本用于初始化全新数据库。
-- 2. 字符集统一使用 utf8mb4。
-- 3. 已直接写入当前项目实际使用的最终表结构，无需再额外执行迁移脚本。

CREATE DATABASE IF NOT EXISTS `youda`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE `youda`;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================
-- 删除旧表
-- =============================================

DROP TABLE IF EXISTS `course_purchase`;
DROP TABLE IF EXISTS `resource_purchase`;
DROP TABLE IF EXISTS `points_record`;
DROP TABLE IF EXISTS `user_checkin`;
DROP TABLE IF EXISTS `wrong_question`;
DROP TABLE IF EXISTS `learning_progress`;
DROP TABLE IF EXISTS `course_video`;
DROP TABLE IF EXISTS `course_chapter`;
DROP TABLE IF EXISTS `course`;
DROP TABLE IF EXISTS `chat_message`;
DROP TABLE IF EXISTS `chat`;
DROP TABLE IF EXISTS `resource`;
DROP TABLE IF EXISTS `post_collect`;
DROP TABLE IF EXISTS `post_like`;
DROP TABLE IF EXISTS `comment`;
DROP TABLE IF EXISTS `post`;
DROP TABLE IF EXISTS `announcement`;
DROP TABLE IF EXISTS `grade`;
DROP TABLE IF EXISTS `subject`;
DROP TABLE IF EXISTS `category`;
DROP TABLE IF EXISTS `user`;

-- =============================================
-- 基础表
-- =============================================

CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码哈希',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `bio` VARCHAR(255) DEFAULT NULL COMMENT '个人简介',
    `points` INT DEFAULT 0 COMMENT '积分余额',
    `virtual_balance` INT DEFAULT 0 COMMENT '虚拟币余额',
    `role` TINYINT DEFAULT 0 COMMENT '角色：0学生，1管理员',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0禁用，1正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE `category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `sort` INT DEFAULT 0 COMMENT '排序值',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0禁用，1启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子分类表';

CREATE TABLE `subject` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '学科ID',
    `name` VARCHAR(50) NOT NULL COMMENT '学科名称',
    `sort` INT DEFAULT 0 COMMENT '排序值',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0禁用，1启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学科表';

CREATE TABLE `grade` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '年级ID',
    `name` VARCHAR(50) NOT NULL COMMENT '年级名称',
    `level` VARCHAR(20) NOT NULL COMMENT '学段',
    `sort` INT DEFAULT 0 COMMENT '排序值',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0禁用，1启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='年级表';

CREATE TABLE `post` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `content` TEXT NOT NULL COMMENT '正文内容',
    `user_id` BIGINT NOT NULL COMMENT '发帖用户ID',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `view_count` INT DEFAULT 0 COMMENT '浏览数',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `comment_count` INT DEFAULT 0 COMMENT '评论数',
    `collect_count` INT DEFAULT 0 COMMENT '收藏数',
    `is_top` TINYINT DEFAULT 0 COMMENT '是否置顶：0否，1是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    PRIMARY KEY (`id`),
    KEY `idx_post_user_id` (`user_id`),
    KEY `idx_post_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子表';

CREATE TABLE `comment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `post_id` BIGINT NOT NULL COMMENT '帖子ID',
    `user_id` BIGINT NOT NULL COMMENT '评论用户ID',
    `content` VARCHAR(500) NOT NULL COMMENT '评论内容',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    PRIMARY KEY (`id`),
    KEY `idx_comment_post_id` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

CREATE TABLE `post_like` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `post_id` BIGINT NOT NULL COMMENT '帖子ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_post_like_user_post` (`user_id`, `post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子点赞记录表';

CREATE TABLE `post_collect` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `post_id` BIGINT NOT NULL COMMENT '帖子ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_post_collect_user_post` (`user_id`, `post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子收藏记录表';

CREATE TABLE `resource` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '资料ID',
    `name` VARCHAR(200) NOT NULL COMMENT '资料名称',
    `description` TEXT COMMENT '资料简介',
    `file_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `file_path` VARCHAR(500) NOT NULL COMMENT '文件存储路径',
    `file_type` VARCHAR(20) NOT NULL COMMENT '文件类型',
    `file_size` BIGINT NOT NULL COMMENT '文件大小（字节）',
    `subject_id` BIGINT NOT NULL COMMENT '学科ID',
    `grade_id` BIGINT NOT NULL COMMENT '年级ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '上传者ID',
    `download_count` INT DEFAULT 0 COMMENT '下载次数',
    `requires_points` TINYINT DEFAULT 0 COMMENT '是否需要积分购买：0否，1是',
    `points_cost` INT DEFAULT 0 COMMENT '积分价格',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    PRIMARY KEY (`id`),
    KEY `idx_resource_subject_id` (`subject_id`),
    KEY `idx_resource_grade_id` (`grade_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资料表';

CREATE TABLE `chat` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `chat_id` VARCHAR(50) NOT NULL COMMENT '会话唯一标识',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `title` VARCHAR(200) DEFAULT NULL COMMENT '会话标题',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_chat_chat_id` (`chat_id`),
    KEY `idx_chat_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 会话表';

CREATE TABLE `chat_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `chat_id` VARCHAR(50) NOT NULL COMMENT '会话唯一标识',
    `role` VARCHAR(20) NOT NULL COMMENT '角色：user / assistant',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `image` VARCHAR(500) DEFAULT NULL COMMENT '附带图片地址',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_chat_message_chat_id` (`chat_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 会话消息表';

CREATE TABLE `course` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '课程ID',
    `name` VARCHAR(200) NOT NULL COMMENT '课程名称',
    `description` TEXT COMMENT '课程简介',
    `cover_image` VARCHAR(500) DEFAULT NULL COMMENT '课程封面',
    `teacher_name` VARCHAR(50) DEFAULT NULL COMMENT '讲师名称',
    `subject_id` BIGINT NOT NULL COMMENT '学科ID',
    `grade_id` BIGINT NOT NULL COMMENT '年级ID',
    `chapter_count` INT DEFAULT 0 COMMENT '章节数',
    `student_count` INT DEFAULT 0 COMMENT '学习人数',
    `requires_points` TINYINT DEFAULT 0 COMMENT '兼容旧版字段：是否积分课',
    `points_cost` INT DEFAULT 0 COMMENT '兼容旧版字段：积分价格',
    `price_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '课程价格（人民币）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    PRIMARY KEY (`id`),
    KEY `idx_course_subject_id` (`subject_id`),
    KEY `idx_course_grade_id` (`grade_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

CREATE TABLE `course_chapter` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '章节ID',
    `course_id` BIGINT NOT NULL COMMENT '课程ID',
    `title` VARCHAR(200) NOT NULL COMMENT '章节标题',
    `sort` INT DEFAULT 0 COMMENT '排序值',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    PRIMARY KEY (`id`),
    KEY `idx_course_chapter_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程章节表';

CREATE TABLE `course_video` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '视频ID',
    `course_id` BIGINT NOT NULL COMMENT '课程ID',
    `chapter_id` BIGINT NOT NULL COMMENT '章节ID',
    `title` VARCHAR(200) NOT NULL COMMENT '视频标题',
    `video_url` VARCHAR(500) NOT NULL COMMENT '视频地址',
    `duration` INT DEFAULT 0 COMMENT '视频时长（秒）',
    `sort` INT DEFAULT 0 COMMENT '排序值',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    PRIMARY KEY (`id`),
    KEY `idx_course_video_chapter_id` (`chapter_id`),
    KEY `idx_course_video_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程视频表';

CREATE TABLE `learning_progress` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `course_id` BIGINT NOT NULL COMMENT '课程ID',
    `video_id` BIGINT NOT NULL COMMENT '视频ID',
    `position` INT DEFAULT 0 COMMENT '播放位置（秒）',
    `is_completed` TINYINT DEFAULT 0 COMMENT '是否完成：0否，1是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_learning_progress_user_video` (`user_id`, `video_id`),
    KEY `idx_learning_progress_user_course` (`user_id`, `course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习进度表';

CREATE TABLE `announcement` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '公告ID',
    `title` VARCHAR(200) NOT NULL COMMENT '公告标题',
    `content` TEXT NOT NULL COMMENT '公告内容',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0下线，1上线',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- =============================================
-- 扩展表
-- =============================================

CREATE TABLE `wrong_question` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '错题ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `subject_id` BIGINT NOT NULL COMMENT '学科ID',
    `grade_id` BIGINT NOT NULL COMMENT '年级ID',
    `question_content` TEXT NOT NULL COMMENT '题目内容',
    `question_image` VARCHAR(500) DEFAULT NULL COMMENT '题目图片',
    `my_answer` TEXT DEFAULT NULL COMMENT '用户作答',
    `correct_answer` TEXT NOT NULL COMMENT '正确答案',
    `error_reason` VARCHAR(255) DEFAULT NULL COMMENT '错因分析',
    `mastery_status` TINYINT DEFAULT 0 COMMENT '掌握状态：0未掌握，1复习中，2已掌握',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    PRIMARY KEY (`id`),
    KEY `idx_wrong_question_user_id` (`user_id`),
    KEY `idx_wrong_question_subject_id` (`subject_id`),
    KEY `idx_wrong_question_grade_id` (`grade_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='错题本表';

CREATE TABLE `user_checkin` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `checkin_date` DATE NOT NULL COMMENT '签到日期',
    `points_reward` INT DEFAULT 0 COMMENT '基础奖励积分',
    `bonus_points` INT DEFAULT 0 COMMENT '额外奖励积分',
    `streak_days` INT DEFAULT 1 COMMENT '连续签到天数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_checkin_user_date` (`user_id`, `checkin_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户签到表';

CREATE TABLE `points_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `action_type` VARCHAR(50) NOT NULL COMMENT '动作类型',
    `biz_id` VARCHAR(64) NOT NULL COMMENT '业务唯一标识',
    `points` INT NOT NULL COMMENT '积分变化值，正数为增加，负数为扣减',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_points_record_user_action_biz` (`user_id`, `action_type`, `biz_id`),
    KEY `idx_points_record_user_time` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分流水表';

CREATE TABLE `resource_purchase` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `resource_id` BIGINT NOT NULL COMMENT '资料ID',
    `points_cost` INT NOT NULL COMMENT '消耗积分',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_resource_purchase_user_resource` (`user_id`, `resource_id`),
    KEY `idx_resource_purchase_user_id` (`user_id`),
    KEY `idx_resource_purchase_resource_id` (`resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资料购买记录表';

CREATE TABLE `course_purchase` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `course_id` BIGINT NOT NULL COMMENT '课程ID',
    `points_cost` INT NOT NULL COMMENT '兼容旧版字段：订单积分价格',
    `payment_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单金额（人民币）',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态：0待支付，1已支付，2已完成',
    `paid_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `completed_time` DATETIME DEFAULT NULL COMMENT '完成时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_course_purchase_order_no` (`order_no`),
    KEY `idx_course_purchase_user_id` (`user_id`),
    KEY `idx_course_purchase_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程订单表';

-- =============================================
-- 基础字典数据
-- =============================================

INSERT INTO `subject` (`id`, `name`, `sort`, `status`) VALUES
(1, '语文', 1, 1),
(2, '数学', 2, 1),
(3, '英语', 3, 1),
(4, '物理', 4, 1),
(5, '化学', 5, 1),
(6, '生物', 6, 1),
(7, '历史', 7, 1),
(8, '地理', 8, 1),
(9, '政治', 9, 1);

INSERT INTO `grade` (`id`, `name`, `level`, `sort`, `status`) VALUES
(1, '一年级', '小学', 1, 1),
(2, '二年级', '小学', 2, 1),
(3, '三年级', '小学', 3, 1),
(4, '四年级', '小学', 4, 1),
(5, '五年级', '小学', 5, 1),
(6, '六年级', '小学', 6, 1),
(7, '初一', '初中', 7, 1),
(8, '初二', '初中', 8, 1),
(9, '初三', '初中', 9, 1),
(10, '高一', '高中', 10, 1),
(11, '高二', '高中', 11, 1),
(12, '高三', '高中', 12, 1);

INSERT INTO `category` (`id`, `name`, `sort`, `status`) VALUES
(1, '综合', 1, 1),
(2, '数学', 2, 1),
(3, '英语', 3, 1),
(4, '物理', 4, 1),
(5, '化学', 5, 1),
(6, '语文', 6, 1),
(7, '生物', 7, 1),
(8, '历史', 8, 1),
(9, '地理', 9, 1),
(10, '政治', 10, 1);

-- =============================================
-- 用户与公告
-- =============================================

INSERT INTO `user` (
    `id`, `username`, `password`, `nickname`, `avatar`, `phone`, `bio`,
    `points`, `virtual_balance`, `role`, `status`
) VALUES
(1, 'admin', '$2a$10$fK5GSp1tp9t4EJVZYzmZeeYO8p7XoLKxi4Y/ZsARHYkgbXWPiD.pO', '管理员', NULL, '13800000000', '平台默认管理员账号', 999, 0, 1, 1),
(2, 'student_zhang', '$2a$10$gPu6w6pQ//3Ry6u9AIaExOzNPtlWv3oxGH9gQ8fIOVUjP9n6puWhG', '张小雨', 'https://upload.wikimedia.org/wikipedia/commons/2/26/Portrait_photography.jpg', '13900000001', '初二学生，擅长整理数学错题。', 42, 120, 0, 1),
(3, 'student_li', '$2a$10$gPu6w6pQ//3Ry6u9AIaExOzNPtlWv3oxGH9gQ8fIOVUjP9n6puWhG', '李明轩', 'https://upload.wikimedia.org/wikipedia/commons/1/1e/PORTRAIT_PICTURE.jpg', '13900000002', '高一学生，正在提升英语写作和阅读。', 35, 90, 0, 1),
(4, 'student_wang', '$2a$10$gPu6w6pQ//3Ry6u9AIaExOzNPtlWv3oxGH9gQ8fIOVUjP9n6puWhG', '王可心', 'https://upload.wikimedia.org/wikipedia/commons/6/6d/Portrait_woman.jpg', '13900000003', '初三学生，常用平台复习物理和化学。', 28, 60, 0, 1),
(5, 'points_tester', '$2a$10$gPu6w6pQ//3Ry6u9AIaExOzNPtlWv3oxGH9gQ8fIOVUjP9n6puWhG', '积分测试员', 'https://upload.wikimedia.org/wikipedia/commons/2/26/Portrait_photography.jpg', '13900000011', '用于测试积分购买资料。', 80, 120, 0, 1),
(6, 'points_buyer', '$2a$10$gPu6w6pQ//3Ry6u9AIaExOzNPtlWv3oxGH9gQ8fIOVUjP9n6puWhG', '订单测试员', 'https://upload.wikimedia.org/wikipedia/commons/1/1e/PORTRAIT_PICTURE.jpg', '13900000012', '用于测试课程下单与支付流程。', 80, 120, 0, 1);

INSERT INTO `announcement` (`id`, `title`, `content`, `status`, `create_time`) VALUES
(1, '欢迎使用优答答疑平台', '欢迎来到优答。你可以在这里提问、学习课程、下载资料并记录错题。建议在提问时补充年级、学科、题目原文和你的思路，这样更容易获得高质量回答。', 1, DATE_SUB(NOW(), INTERVAL 15 DAY)),
(2, '平台使用说明', '课程、资料和社区模块都已开启。部分资料和课程需要购买后解锁，请先登录账号，再进行积分兑换或课程下单。', 1, DATE_SUB(NOW(), INTERVAL 10 DAY));

-- =============================================
-- 社区示例数据
-- =============================================

INSERT INTO `post` (
    `id`, `title`, `content`, `user_id`, `category_id`,
    `view_count`, `like_count`, `comment_count`, `collect_count`, `is_top`,
    `create_time`, `update_time`
) VALUES
(1, '初二数学：一次函数总是看图就乱，怎么快速判断增减性？', '我现在一看到一次函数图像就紧张，尤其是题目把文字条件和图像放在一起的时候，总担心符号判断错。有没有固定步骤可以先看斜率、再看截距，最后再判断增减性？', 2, 2, 126, 2, 2, 1, 1, DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 20 DAY)),
(2, '英语作文总写不长，怎样把句子写得更完整？', '每次写英语作文都只有几句简单句，老师说内容太单薄。我想知道平时应该怎么积累连接词和扩写句型，才能把一段话写完整。', 3, 3, 84, 1, 1, 1, 0, DATE_SUB(NOW(), INTERVAL 19 DAY), DATE_SUB(NOW(), INTERVAL 19 DAY)),
(3, '物理受力分析总漏力，大家有什么检查方法？', '做受力分析题时，我经常把支持力和摩擦力漏掉，尤其是斜面和多个物体相连的时候。有没有一套从研究对象到画图标注的检查顺序？', 4, 4, 56, 1, 1, 1, 0, DATE_SUB(NOW(), INTERVAL 18 DAY), DATE_SUB(NOW(), INTERVAL 18 DAY)),
(4, '平台答疑区发帖规范说明', '提问时建议补充年级、学科、题目原文和自己的思路。这样更容易得到准确、可执行的解答，也方便其他同学检索相似问题。', 1, 1, 32, 0, 0, 0, 0, DATE_SUB(NOW(), INTERVAL 17 DAY), DATE_SUB(NOW(), INTERVAL 17 DAY));

INSERT INTO `comment` (`id`, `post_id`, `user_id`, `content`, `create_time`) VALUES
(1, 1, 3, '先判断斜率 k 的正负，再看图像是左低右高还是左高右低，最后结合题目给的点去验证，一般不会错。', DATE_SUB(NOW(), INTERVAL 19 DAY)),
(2, 1, 4, '我会先画一个最简单的坐标系，把题目里的关键点标出来，再决定增减性，这样会稳很多。', DATE_SUB(NOW(), INTERVAL 19 DAY)),
(3, 2, 2, '可以准备一个连接词小卡片，比如 firstly、in addition、as a result，写作时很实用。', DATE_SUB(NOW(), INTERVAL 18 DAY)),
(4, 3, 2, '建议每次都先写研究对象，再列接触面和外力来源，尤其别忘了支持力和摩擦力。', DATE_SUB(NOW(), INTERVAL 17 DAY));

INSERT INTO `post_like` (`id`, `user_id`, `post_id`, `create_time`) VALUES
(1, 3, 1, DATE_SUB(NOW(), INTERVAL 19 DAY)),
(2, 4, 1, DATE_SUB(NOW(), INTERVAL 19 DAY)),
(3, 2, 2, DATE_SUB(NOW(), INTERVAL 18 DAY)),
(4, 3, 3, DATE_SUB(NOW(), INTERVAL 17 DAY));

INSERT INTO `post_collect` (`id`, `user_id`, `post_id`, `create_time`) VALUES
(1, 3, 1, DATE_SUB(NOW(), INTERVAL 19 DAY)),
(2, 2, 2, DATE_SUB(NOW(), INTERVAL 18 DAY)),
(3, 4, 2, DATE_SUB(NOW(), INTERVAL 18 DAY)),
(4, 2, 3, DATE_SUB(NOW(), INTERVAL 17 DAY));

-- =============================================
-- 资料与 AI 对话数据
-- =============================================

INSERT INTO `resource` (
    `id`, `name`, `description`, `file_name`, `file_path`, `file_type`, `file_size`,
    `subject_id`, `grade_id`, `user_id`, `download_count`, `requires_points`, `points_cost`, `create_time`
) VALUES
(1, '一次函数知识清单', '适合初二复习的一次函数核心考点总结，包含斜率、截距、图像判定和常见陷阱。', 'math-function-summary.txt', 'uploads/resource/demo/math-function-summary.txt', 'txt', 2048, 2, 8, 2, 15, 0, 0, DATE_SUB(NOW(), INTERVAL 16 DAY)),
(2, '英语作文扩写连接词模板', '整理了英文写作中常见的连接词、扩写句型和段落衔接表达，适合高一到高三使用。', 'english-writing-links.txt', 'uploads/resource/demo/english-writing-links.txt', 'txt', 1536, 3, 10, 3, 11, 1, 6, DATE_SUB(NOW(), INTERVAL 15 DAY)),
(3, '物理受力分析检查表', '按研究对象、受力来源、方向判断和易漏项四个步骤设计的清单，便于错题复盘。', 'physics-force-template.txt', 'uploads/resource/demo/physics-force-template.txt', 'txt', 1890, 4, 9, 4, 9, 1, 14, DATE_SUB(NOW(), INTERVAL 14 DAY)),
(4, '化学实验现象速记表', '把常见初中化学实验现象整理成一页表格，适合考前速查。', 'chemistry-lab-observation.txt', 'uploads/resource/demo/chemistry-lab-observation.txt', 'txt', 1720, 5, 9, 1, 6, 0, 0, DATE_SUB(NOW(), INTERVAL 13 DAY));

INSERT INTO `chat` (`id`, `chat_id`, `user_id`, `title`, `create_time`, `update_time`) VALUES
(1, 'chat_seed_001', 2, '一次函数看图题怎么拆解', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, 'chat_seed_002', 3, '英语作文如何扩写句子', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY));

INSERT INTO `chat_message` (`id`, `chat_id`, `role`, `content`, `create_time`) VALUES
(1, 'chat_seed_001', 'user', '一次函数图像题总是分不清增减性和截距的作用，应该先看什么？', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, 'chat_seed_001', 'assistant', '先看斜率判断增减性，再看与坐标轴交点，最后把题目给出的点代入验证。按这三个步骤做，图像题会稳定很多。', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(3, 'chat_seed_002', 'user', '英语作文只有短句，怎么把一段话写得更完整？', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(4, 'chat_seed_002', 'assistant', '可以先补充原因、结果和例子，再加入连接词，例如 firstly、besides、therefore。这样一段话会更自然也更充实。', DATE_SUB(NOW(), INTERVAL 3 DAY));

-- =============================================
-- 课程与学习进度数据
-- =============================================

INSERT INTO `course` (
    `id`, `name`, `description`, `cover_image`, `teacher_name`, `subject_id`, `grade_id`,
    `chapter_count`, `student_count`, `requires_points`, `points_cost`, `price_amount`,
    `create_time`, `update_time`
) VALUES
(1, 'MIT 开放课程：固体化学', '整理自公开课资源，适合高中阶段对化学有拓展兴趣的同学。课程内容偏启发式，适合打基础和开阔视野。', '/uploads/course-cover/mit-chemistry.svg', '唐纳德·萨多维', 5, 12, 2, 5, 0, 0, 0.00, DATE_SUB(NOW(), INTERVAL 22 DAY), DATE_SUB(NOW(), INTERVAL 12 DAY)),
(2, 'MIT 开放课程：非线性规划', '聚焦高中数学中的函数、优化和建模思维，适合想系统提升分析能力的同学。', '/uploads/course-cover/mit-nonlinear.svg', '罗伯特·弗伦德', 2, 12, 2, 5, 1, 12, 16.00, DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY)),
(3, '初中物理受力分析专项提升', '围绕受力分析、斜面问题和多物体系统设计的专题课程，适合初三冲刺和错题复盘。', 'https://upload.wikimedia.org/wikipedia/commons/4/4e/Physics_classroom.jpg', '林老师', 4, 9, 1, 1, 1, 16, 16.00, DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY));

INSERT INTO `course_chapter` (`id`, `course_id`, `title`, `sort`, `create_time`) VALUES
(1, 1, '固体结构基础', 1, DATE_SUB(NOW(), INTERVAL 22 DAY)),
(2, 1, '化学键与材料性质', 2, DATE_SUB(NOW(), INTERVAL 21 DAY)),
(3, 2, '函数优化入门', 1, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(4, 2, '约束条件与最值', 2, DATE_SUB(NOW(), INTERVAL 19 DAY)),
(5, 3, '受力分析四步法', 1, DATE_SUB(NOW(), INTERVAL 12 DAY));

INSERT INTO `course_video` (`id`, `course_id`, `chapter_id`, `title`, `video_url`, `duration`, `sort`, `create_time`) VALUES
(1, 1, 1, '晶体结构与晶胞概念', 'https://filesamples.com/samples/video/mp4/sample_640x360.mp4', 13, 1, DATE_SUB(NOW(), INTERVAL 22 DAY)),
(2, 1, 2, '材料性质与化学键关系', 'https://filesamples.com/samples/video/mp4/sample_960x540.mp4', 13, 1, DATE_SUB(NOW(), INTERVAL 21 DAY)),
(3, 2, 3, '非线性规划的基本思路', 'https://disk.sample.cat/samples/mp4/1416529-sd_640_360_30fps.mp4', 12, 1, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(4, 2, 4, '约束最值题怎么建模', 'https://disk.sample.cat/samples/mp4/1416529-sd_960_540_30fps.mp4', 12, 1, DATE_SUB(NOW(), INTERVAL 19 DAY)),
(5, 3, 5, '受力分析专项训练', 'https://filesamples.com/samples/video/mp4/sample_640x360.mp4', 13, 1, DATE_SUB(NOW(), INTERVAL 12 DAY));

INSERT INTO `learning_progress` (
    `id`, `user_id`, `course_id`, `video_id`, `position`, `is_completed`, `create_time`, `update_time`
) VALUES
(1, 2, 1, 1, 13, 1, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, 2, 1, 2, 7, 0, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
(3, 3, 2, 3, 12, 1, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY));

-- =============================================
-- 错题本与签到积分数据
-- =============================================

INSERT INTO `wrong_question` (
    `id`, `user_id`, `subject_id`, `grade_id`, `question_content`, `question_image`,
    `my_answer`, `correct_answer`, `error_reason`, `mastery_status`, `create_time`, `update_time`
) VALUES
(1, 2, 2, 8, '已知一次函数 y = 2x - 3，当 x = 4 时，求 y 的值，并判断函数的增减性。', NULL, '我算出 y = 5，但不确定增减性。', '当 x = 4 时，y = 5；因为 k = 2 > 0，所以函数单调递增。', '斜率和代值都做对了，但没有把“k 大于 0”与图像左低右高联系起来。', 0, DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY)),
(2, 3, 3, 10, '请把句子 I like sports. 扩写成两句更完整的英语表达。', NULL, 'I like sports because it good.', 'I like sports very much. They help me stay healthy and make me feel relaxed.', '连接词和谓语搭配不准确，句子扩写时缺少原因和结果。', 1, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
(3, 4, 4, 9, '斜面上的木块静止不动，请分析它受到哪些力。', NULL, '只写了重力和支持力。', '木块受到重力、支持力，以及沿接触面方向的静摩擦力。', '漏掉了静摩擦力，说明没有先判断接触面和相对运动趋势。', 2, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY));

INSERT INTO `user_checkin` (
    `id`, `user_id`, `checkin_date`, `points_reward`, `bonus_points`, `streak_days`, `create_time`
) VALUES
(1, 2, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 5, 0, 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 3, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 5, 0, 1, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 4, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 5, 0, 1, DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT INTO `points_record` (
    `id`, `user_id`, `action_type`, `biz_id`, `points`, `remark`, `create_time`
) VALUES
(1, 2, 'CHECKIN', DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '%Y-%m-%d'), 5, '每日签到', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 2, 'POST_CREATE', '1', 3, '发布帖子', DATE_SUB(NOW(), INTERVAL 20 DAY)),
(3, 2, 'COMMENT_CREATE', '4', 1, '发表评论', DATE_SUB(NOW(), INTERVAL 17 DAY)),
(4, 2, 'WRONG_QUESTION_CREATE', '1', 2, '添加错题', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(5, 2, 'RESOURCE_PURCHASE', '2', -6, '购买资料：英语作文扩写连接词模板', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(6, 3, 'CHECKIN', DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '%Y-%m-%d'), 5, '每日签到', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(7, 3, 'POST_CREATE', '2', 3, '发布帖子', DATE_SUB(NOW(), INTERVAL 19 DAY)),
(8, 3, 'COMMENT_CREATE', '1', 1, '发表评论', DATE_SUB(NOW(), INTERVAL 19 DAY)),
(9, 3, 'WRONG_QUESTION_CREATE', '2', 2, '添加错题', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(10, 3, 'CHAPTER_COMPLETE', '3', 10, '完成课程章节', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(11, 4, 'CHECKIN', DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '%Y-%m-%d'), 5, '每日签到', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(12, 4, 'POST_CREATE', '3', 3, '发布帖子', DATE_SUB(NOW(), INTERVAL 18 DAY)),
(13, 4, 'COMMENT_CREATE', '2', 1, '发表评论', DATE_SUB(NOW(), INTERVAL 19 DAY)),
(14, 4, 'WRONG_QUESTION_CREATE', '3', 2, '添加错题', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(15, 5, 'ADMIN_GRANT', 'seed-points-tester', 80, '初始化测试积分', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(16, 6, 'ADMIN_GRANT', 'seed-points-buyer', 80, '初始化测试积分', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- =============================================
-- 购买记录与订单数据
-- =============================================

INSERT INTO `resource_purchase` (`id`, `user_id`, `resource_id`, `points_cost`, `create_time`) VALUES
(1, 2, 2, 6, DATE_SUB(NOW(), INTERVAL 5 DAY));

INSERT INTO `course_purchase` (
    `id`, `order_no`, `user_id`, `course_id`, `points_cost`, `payment_amount`,
    `status`, `paid_time`, `completed_time`, `create_time`
) VALUES
(1, 'COURSE202604120830001001', 2, 2, 12, 16.00, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), NULL, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 'COURSE2026041212353013468', 1, 3, 16, 16.00, 2, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 'COURSE202604130930001203', 3, 3, 16, 16.00, 0, NULL, NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR));

SET FOREIGN_KEY_CHECKS = 1;
