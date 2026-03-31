-- 优答·中小学答疑网 数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS `youda` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `youda`;
SET NAMES utf8mb4;

-- =============================================
-- 1. 用户表
-- =============================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
    `role` TINYINT DEFAULT 0 COMMENT '角色：0用户 1管理员',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0禁用 1正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =============================================
-- 2. 帖子分类表
-- =============================================
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0禁用 1正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子分类表';

-- =============================================
-- 3. 帖子表
-- =============================================
DROP TABLE IF EXISTS `post`;
CREATE TABLE `post` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `content` TEXT NOT NULL COMMENT '内容',
    `user_id` BIGINT NOT NULL COMMENT '作者ID',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `view_count` INT DEFAULT 0 COMMENT '浏览数',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `comment_count` INT DEFAULT 0 COMMENT '评论数',
    `collect_count` INT DEFAULT 0 COMMENT '收藏数',
    `is_top` TINYINT DEFAULT 0 COMMENT '是否置顶',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子表';

-- =============================================
-- 4. 评论表
-- =============================================
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `post_id` BIGINT NOT NULL COMMENT '帖子ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `content` VARCHAR(500) NOT NULL COMMENT '评论内容',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_post_id` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- =============================================
-- 5. 点赞表
-- =============================================
DROP TABLE IF EXISTS `post_like`;
CREATE TABLE `post_like` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `post_id` BIGINT NOT NULL COMMENT '帖子ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_post` (`user_id`, `post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞表';

-- =============================================
-- 6. 收藏表
-- =============================================
DROP TABLE IF EXISTS `post_collect`;
CREATE TABLE `post_collect` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `post_id` BIGINT NOT NULL COMMENT '帖子ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_post` (`user_id`, `post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- =============================================
-- 7. 学科表
-- =============================================
DROP TABLE IF EXISTS `subject`;
CREATE TABLE `subject` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '学科ID',
    `name` VARCHAR(50) NOT NULL COMMENT '学科名称',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0禁用 1正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学科表';

-- =============================================
-- 8. 年级表
-- =============================================
DROP TABLE IF EXISTS `grade`;
CREATE TABLE `grade` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '年级ID',
    `name` VARCHAR(50) NOT NULL COMMENT '年级名称',
    `level` VARCHAR(20) NOT NULL COMMENT '学段',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0禁用 1正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='年级表';

-- =============================================
-- 9. 学习资料表
-- =============================================
DROP TABLE IF EXISTS `resource`;
CREATE TABLE `resource` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '资料ID',
    `name` VARCHAR(200) NOT NULL COMMENT '资料名称',
    `description` TEXT COMMENT '资料描述',
    `file_name` VARCHAR(255) NOT NULL COMMENT '文件名',
    `file_path` VARCHAR(500) NOT NULL COMMENT '文件路径',
    `file_type` VARCHAR(20) NOT NULL COMMENT '文件类型',
    `file_size` BIGINT NOT NULL COMMENT '文件大小',
    `subject_id` BIGINT NOT NULL COMMENT '学科ID',
    `grade_id` BIGINT NOT NULL COMMENT '年级ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '上传者ID',
    `download_count` INT DEFAULT 0 COMMENT '下载次数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_subject_id` (`subject_id`),
    KEY `idx_grade_id` (`grade_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习资料表';

-- =============================================
-- 10. AI对话表
-- =============================================
DROP TABLE IF EXISTS `chat`;
CREATE TABLE `chat` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `chat_id` VARCHAR(50) NOT NULL COMMENT '对话ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `title` VARCHAR(200) DEFAULT NULL COMMENT '对话标题',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_chat_id` (`chat_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话表';

-- =============================================
-- 11. 对话消息表
-- =============================================
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `chat_id` VARCHAR(50) NOT NULL COMMENT '对话ID',
    `role` VARCHAR(20) NOT NULL COMMENT '角色：user/assistant',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `image` VARCHAR(500) DEFAULT NULL COMMENT '图片路径',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_chat_id` (`chat_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话消息表';

-- =============================================
-- 12. 课程表
-- =============================================
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '课程ID',
    `name` VARCHAR(200) NOT NULL COMMENT '课程名称',
    `description` TEXT COMMENT '课程描述',
    `cover_image` VARCHAR(500) DEFAULT NULL COMMENT '封面图',
    `teacher_name` VARCHAR(50) DEFAULT NULL COMMENT '教师名称',
    `subject_id` BIGINT NOT NULL COMMENT '学科ID',
    `grade_id` BIGINT NOT NULL COMMENT '年级ID',
    `chapter_count` INT DEFAULT 0 COMMENT '章节数',
    `student_count` INT DEFAULT 0 COMMENT '学习人数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_subject_id` (`subject_id`),
    KEY `idx_grade_id` (`grade_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- =============================================
-- 13. 课程章节表
-- =============================================
DROP TABLE IF EXISTS `course_chapter`;
CREATE TABLE `course_chapter` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '章节ID',
    `course_id` BIGINT NOT NULL COMMENT '课程ID',
    `title` VARCHAR(200) NOT NULL COMMENT '章节标题',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程章节表';

-- =============================================
-- 14. 课程视频表
-- =============================================
DROP TABLE IF EXISTS `course_video`;
CREATE TABLE `course_video` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '视频ID',
    `course_id` BIGINT NOT NULL COMMENT '课程ID',
    `chapter_id` BIGINT NOT NULL COMMENT '章节ID',
    `title` VARCHAR(200) NOT NULL COMMENT '视频标题',
    `video_url` VARCHAR(500) NOT NULL COMMENT '视频地址',
    `duration` INT DEFAULT 0 COMMENT '时长(秒)',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_chapter_id` (`chapter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程视频表';

-- =============================================
-- 15. 学习进度表
-- =============================================
DROP TABLE IF EXISTS `learning_progress`;
CREATE TABLE `learning_progress` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `course_id` BIGINT NOT NULL COMMENT '课程ID',
    `video_id` BIGINT NOT NULL COMMENT '视频ID',
    `position` INT DEFAULT 0 COMMENT '播放位置(秒)',
    `is_completed` TINYINT DEFAULT 0 COMMENT '是否完成',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_video` (`user_id`, `video_id`),
    KEY `idx_user_course` (`user_id`, `course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习进度表';

-- =============================================
-- 16. 公告表
-- =============================================
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '公告ID',
    `title` VARCHAR(200) NOT NULL COMMENT '公告标题',
    `content` TEXT NOT NULL COMMENT '公告内容',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0禁用 1正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- =============================================
-- 初始化数据
-- =============================================

-- 管理员账号（密码：admin123）
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`) VALUES
('admin', '$2a$10$fK5GSp1tp9t4EJVZYzmZeeYO8p7XoLKxi4Y/ZsARHYkgbXWPiD.pO', '管理员', 1, 1);

-- 学科数据
INSERT INTO `subject` (`name`, `sort`, `status`) VALUES
('语文', 1, 1),
('数学', 2, 1),
('英语', 3, 1),
('物理', 4, 1),
('化学', 5, 1),
('生物', 6, 1),
('历史', 7, 1),
('地理', 8, 1),
('政治', 9, 1);

-- 年级数据
INSERT INTO `grade` (`name`, `level`, `sort`, `status`) VALUES
('一年级', '小学', 1, 1),
('二年级', '小学', 2, 1),
('三年级', '小学', 3, 1),
('四年级', '小学', 4, 1),
('五年级', '小学', 5, 1),
('六年级', '小学', 6, 1),
('初一', '初中', 7, 1),
('初二', '初中', 8, 1),
('初三', '初中', 9, 1),
('高一', '高中', 10, 1),
('高二', '高中', 11, 1),
('高三', '高中', 12, 1);

-- 帖子分类数据
INSERT INTO `category` (`name`, `sort`, `status`) VALUES
('语文', 1, 1),
('数学', 2, 1),
('英语', 3, 1),
('物理', 4, 1),
('化学', 5, 1),
('生物', 6, 1),
('历史', 7, 1),
('地理', 8, 1),
('政治', 9, 1),
('综合', 10, 1);

-- 测试公告
INSERT INTO `announcement` (`title`, `content`, `status`) VALUES
('欢迎使用优答答疑网', '优答·中小学答疑网正式上线啦！在这里你可以提问、学习、交流，快来体验吧！', 1),
('平台使用须知', '请文明发言，互相尊重，共同营造良好的学习氛围。', 1);

-- notebook and points extension
ALTER TABLE `user`
    ADD COLUMN `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    ADD COLUMN `bio` VARCHAR(255) DEFAULT NULL COMMENT '个人简介',
    ADD COLUMN `points` INT DEFAULT 0 COMMENT '积分';

DROP TABLE IF EXISTS `wrong_question`;
CREATE TABLE `wrong_question` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `subject_id` BIGINT NOT NULL,
    `grade_id` BIGINT NOT NULL,
    `question_content` TEXT NOT NULL,
    `question_image` VARCHAR(500) DEFAULT NULL,
    `my_answer` TEXT DEFAULT NULL,
    `correct_answer` TEXT NOT NULL,
    `error_reason` VARCHAR(255) DEFAULT NULL,
    `mastery_status` TINYINT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_wrong_question_user` (`user_id`),
    KEY `idx_wrong_question_subject` (`subject_id`),
    KEY `idx_wrong_question_grade` (`grade_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='错题本';

DROP TABLE IF EXISTS `user_checkin`;
CREATE TABLE `user_checkin` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `checkin_date` DATE NOT NULL,
    `points_reward` INT DEFAULT 0,
    `bonus_points` INT DEFAULT 0,
    `streak_days` INT DEFAULT 1,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_checkin_date` (`user_id`, `checkin_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户签到记录';

DROP TABLE IF EXISTS `points_record`;
CREATE TABLE `points_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `action_type` VARCHAR(50) NOT NULL,
    `biz_id` VARCHAR(64) NOT NULL,
    `points` INT NOT NULL,
    `remark` VARCHAR(255) DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_action_biz` (`user_id`, `action_type`, `biz_id`),
    KEY `idx_points_record_user_time` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分明细';

-- =============================================
-- 扩展测试数据
-- 所有测试账号默认密码：admin123
-- =============================================

-- 补充管理员资料
UPDATE `user`
SET `phone` = '13800000000',
    `bio` = '平台默认管理员账号',
    `points` = 999
WHERE `username` = 'admin';

-- 普通测试用户
INSERT INTO `user` (`username`, `password`, `nickname`, `avatar`, `phone`, `bio`, `points`, `role`, `status`) VALUES
('student_zhang', '$2a$10$fK5GSp1tp9t4EJVZYzmZeeYO8p7XoLKxi4Y/ZsARHYkgbXWPiD.pO', '张小雨', 'https://upload.wikimedia.org/wikipedia/commons/2/26/Portrait_photography.jpg', '13900000001', '初二学生，喜欢数学和物理', 21, 0, 1),
('student_li', '$2a$10$fK5GSp1tp9t4EJVZYzmZeeYO8p7XoLKxi4Y/ZsARHYkgbXWPiD.pO', '李明轩', 'https://upload.wikimedia.org/wikipedia/commons/1/1e/PORTRAIT_PICTURE.jpg', '13900000002', '高一学生，正在准备英语作文提升', 21, 0, 1),
('student_wang', '$2a$10$fK5GSp1tp9t4EJVZYzmZeeYO8p7XoLKxi4Y/ZsARHYkgbXWPiD.pO', '王可心', 'https://upload.wikimedia.org/wikipedia/commons/6/6d/Portrait_woman.jpg', '13900000003', '初三学生，常整理物理和化学错题', 11, 0, 1);

-- 测试帖子
INSERT INTO `post` (`title`, `content`, `user_id`, `category_id`, `view_count`, `like_count`, `comment_count`, `collect_count`, `is_top`, `create_time`, `update_time`) VALUES
('初二数学：一次函数总是看图就乱，怎么快速判断增减性？', '我现在一看到一次函数图像就紧张，尤其是题目把文字条件和图像放在一起的时候，总担心符号判断错。有没有固定步骤可以先看斜率、再看截距，最后再判断增减性？', 2, 2, 126, 2, 2, 1, 1, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
('英语作文总写不长，怎样把句子写得更完整？', '每次写英语作文都只有几句简单句，老师说内容太单薄。我想知道平时应该怎么积累连接词和扩写句型，才能把一段话写完整。', 3, 3, 84, 1, 1, 2, 0, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
('物理受力分析总漏力，大家有什么检查方法？', '做受力分析题时，我经常把支持力和摩擦力漏掉，尤其是斜面和多个物体相连的时候。有没有一套从研究对象到画图标注的检查顺序？', 4, 4, 56, 1, 1, 1, 0, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
('平台答疑区发帖规范说明', '提问时建议补充年级、学科、题目原文和自己的思路。这样更容易得到准确、可执行的解答，也方便其他同学检索相似问题。', 1, 10, 32, 0, 0, 0, 0, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY));

-- 测试评论
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `create_time`) VALUES
(1, 3, '可以先看图像是从左下到右上还是从左上到右下，再判断 k 的正负，最后再结合题目给出的交点信息。', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(1, 4, '我自己会先写一句：x 增大时，y 是否随之增大。把文字翻成图像语言后，就不容易乱。', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(2, 2, '作文可以先准备几个万能连接词，比如 firstly、in addition、as a result，再把原因和结果各补一句。', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(3, 1, '受力分析最关键的是先选研究对象，再把接触面一个个检查，很多漏力都是因为没有逐个检查接触关系。', DATE_SUB(NOW(), INTERVAL 2 DAY));

-- 测试点赞
INSERT INTO `post_like` (`user_id`, `post_id`, `create_time`) VALUES
(3, 1, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(4, 1, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(2, 2, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(3, 3, DATE_SUB(NOW(), INTERVAL 2 DAY));

-- 测试收藏
INSERT INTO `post_collect` (`user_id`, `post_id`, `create_time`) VALUES
(3, 1, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(2, 2, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(4, 2, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 3, DATE_SUB(NOW(), INTERVAL 2 DAY));

-- 测试资料
INSERT INTO `resource` (`name`, `description`, `file_name`, `file_path`, `file_type`, `file_size`, `subject_id`, `grade_id`, `user_id`, `download_count`, `create_time`) VALUES
('初中数学函数基础整理', '适合初一到初二复习函数基础概念、图像与常见题型。', 'math-function-summary.txt', 'uploads/resource/demo/math-function-summary.txt', 'txt', 2048, 2, 8, 2, 15, DATE_SUB(NOW(), INTERVAL 6 DAY)),
('高中英语作文连接词清单', '整理了常见作文衔接词和句式替换表达。', 'english-writing-links.txt', 'uploads/resource/demo/english-writing-links.txt', 'txt', 1536, 3, 10, 3, 11, DATE_SUB(NOW(), INTERVAL 5 DAY)),
('初中物理受力分析模板', '包含受力分析步骤、典型错误和自检清单。', 'physics-force-template.txt', 'uploads/resource/demo/physics-force-template.txt', 'txt', 1890, 4, 9, 4, 9, DATE_SUB(NOW(), INTERVAL 4 DAY));

-- 测试 AI 对话
INSERT INTO `chat` (`chat_id`, `user_id`, `title`, `create_time`, `update_time`) VALUES
('chat_seed_001', 2, '二次函数求最值', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
('chat_seed_002', 3, '英语作文润色建议', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT INTO `chat_message` (`chat_id`, `role`, `content`, `create_time`) VALUES
('chat_seed_001', 'user', '老师，这道二次函数题我不会求最值，可以先告诉我应该看顶点还是配方吗？', DATE_SUB(NOW(), INTERVAL 2 DAY)),
('chat_seed_001', 'assistant', '先看函数解析式是否方便直接配方；如果已经是顶点式，就直接根据顶点坐标判断最值。', DATE_SUB(NOW(), INTERVAL 2 DAY)),
('chat_seed_002', 'user', '请帮我把这段英语作文开头改得更自然一些。', DATE_SUB(NOW(), INTERVAL 1 DAY)),
('chat_seed_002', 'assistant', '可以先给出主题句，再补充原因和个人感受，这样会比单独一句观点更完整。', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 测试课程
INSERT INTO `course` (`name`, `description`, `cover_image`, `teacher_name`, `subject_id`, `grade_id`, `chapter_count`, `student_count`, `create_time`, `update_time`) VALUES
('初中数学函数专项突破', '围绕函数图像、解析式和应用题建立完整解题步骤。', 'https://upload.wikimedia.org/wikipedia/commons/8/84/Learning_Mathematics.jpg', '周老师', 2, 8, 2, 2, DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
('高中英语写作表达提升', '从句式扩写、连接词使用到作文结构搭建的系统课程。', 'https://upload.wikimedia.org/wikipedia/commons/6/60/A_book.jpg', '陈老师', 3, 10, 2, 1, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT INTO `course_chapter` (`course_id`, `title`, `sort`, `create_time`) VALUES
(1, '函数图像与增减性', 1, DATE_SUB(NOW(), INTERVAL 12 DAY)),
(1, '待定系数法与应用题', 2, DATE_SUB(NOW(), INTERVAL 11 DAY)),
(2, '作文开头与主题句', 1, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(2, '连接词与段落扩写', 2, DATE_SUB(NOW(), INTERVAL 9 DAY));

INSERT INTO `course_video` (`course_id`, `chapter_id`, `title`, `video_url`, `duration`, `sort`, `create_time`) VALUES
(1, 1, '用图像判断一次函数性质', 'https://filesamples.com/samples/video/mp4/sample_640x360.mp4', 13, 1, DATE_SUB(NOW(), INTERVAL 12 DAY)),
(1, 2, '函数应用题常见设问拆解', 'https://filesamples.com/samples/video/mp4/sample_960x540.mp4', 13, 1, DATE_SUB(NOW(), INTERVAL 11 DAY)),
(2, 3, '英语作文高分开头怎么写', 'https://disk.sample.cat/samples/mp4/1416529-sd_640_360_30fps.mp4', 12, 1, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(2, 4, '连接词让段落更连贯', 'https://disk.sample.cat/samples/mp4/1416529-sd_960_540_30fps.mp4', 12, 1, DATE_SUB(NOW(), INTERVAL 9 DAY));

INSERT INTO `learning_progress` (`user_id`, `course_id`, `video_id`, `position`, `is_completed`, `create_time`, `update_time`) VALUES
(2, 1, 1, 13, 1, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 1, 2, 7, 0, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 2, 3, 12, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 测试错题本
INSERT INTO `wrong_question` (`user_id`, `subject_id`, `grade_id`, `question_content`, `my_answer`, `correct_answer`, `error_reason`, `mastery_status`, `create_time`, `update_time`) VALUES
(2, 2, 8, '已知一次函数 y = 2x - 3，求当 x = 4 时 y 的值，并判断函数增减性。', '我算出 y = 5，但是不确定函数是不是递减。', '当 x = 4 时，y = 5；因为 k = 2 > 0，所以函数递增。', '把斜率正负和增减性对应关系记混了。', 0, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
(3, 3, 10, '请将句子 I like sports. 扩写成两句相关、连贯的英语表达。', 'I like sports because it good.', 'I like sports very much. They help me stay healthy and make me feel relaxed.', '谓语形式和原因句表达不完整。', 1, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 4, 9, '一个木块静止在斜面上，请分析它受到的力。', '只有重力和支持力。', '木块受到重力、斜面的支持力，以及沿斜面向上的静摩擦力。', '忽略了阻碍下滑趋势的摩擦力。', 2, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 测试签到记录
INSERT INTO `user_checkin` (`user_id`, `checkin_date`, `points_reward`, `bonus_points`, `streak_days`, `create_time`) VALUES
(2, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 5, 0, 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 5, 0, 1, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 5, 0, 1, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 测试积分明细
INSERT INTO `points_record` (`user_id`, `action_type`, `biz_id`, `points`, `remark`, `create_time`) VALUES
(2, 'CHECKIN', DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '%Y-%m-%d'), 5, '每日签到', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 'POST_CREATE', '1', 3, '发布帖子', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, 'COMMENT_CREATE', '3', 1, '发表评论', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 'WRONG_QUESTION_CREATE', '1', 2, '添加错题', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 'CHAPTER_COMPLETE', '1', 10, '完成课程章节', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 'CHECKIN', DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '%Y-%m-%d'), 5, '每日签到', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 'POST_CREATE', '2', 3, '发布帖子', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(3, 'COMMENT_CREATE', '1', 1, '发表评论', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(3, 'WRONG_QUESTION_CREATE', '2', 2, '添加错题', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 'CHAPTER_COMPLETE', '3', 10, '完成课程章节', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 'CHECKIN', DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '%Y-%m-%d'), 5, '每日签到', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 'POST_CREATE', '3', 3, '发布帖子', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(4, 'COMMENT_CREATE', '2', 1, '发表评论', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(4, 'WRONG_QUESTION_CREATE', '3', 2, '添加错题', DATE_SUB(NOW(), INTERVAL 1 DAY));


