-- 浼樼瓟路涓皬瀛︾瓟鐤戠綉 鏁版嵁搴撳垵濮嬪寲鑴氭湰
CREATE DATABASE IF NOT EXISTS `youda` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `youda`;
SET NAMES utf8mb4;

-- =============================================
-- 1. 鐢ㄦ埛琛?-- =============================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '鐢ㄦ埛ID',
    `username` VARCHAR(50) NOT NULL COMMENT '鐢ㄦ埛鍚?,
    `password` VARCHAR(100) NOT NULL COMMENT '瀵嗙爜',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '鏄电О',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '澶村儚',
    `role` TINYINT DEFAULT 0 COMMENT '瑙掕壊锛?鐢ㄦ埛 1绠＄悊鍛?,
    `status` TINYINT DEFAULT 1 COMMENT '鐘舵€侊細0绂佺敤 1姝ｅ父',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '鏄惁鍒犻櫎',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鐢ㄦ埛琛?;

-- =============================================
-- 2. 甯栧瓙鍒嗙被琛?-- =============================================
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '鍒嗙被ID',
    `name` VARCHAR(50) NOT NULL COMMENT '鍒嗙被鍚嶇О',
    `sort` INT DEFAULT 0 COMMENT '鎺掑簭',
    `status` TINYINT DEFAULT 1 COMMENT '鐘舵€侊細0绂佺敤 1姝ｅ父',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='甯栧瓙鍒嗙被琛?;

-- =============================================
-- 3. 甯栧瓙琛?-- =============================================
DROP TABLE IF EXISTS `post`;
CREATE TABLE `post` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '甯栧瓙ID',
    `title` VARCHAR(200) NOT NULL COMMENT '鏍囬',
    `content` TEXT NOT NULL COMMENT '鍐呭',
    `user_id` BIGINT NOT NULL COMMENT '浣滆€匢D',
    `category_id` BIGINT NOT NULL COMMENT '鍒嗙被ID',
    `view_count` INT DEFAULT 0 COMMENT '娴忚鏁?,
    `like_count` INT DEFAULT 0 COMMENT '鐐硅禐鏁?,
    `comment_count` INT DEFAULT 0 COMMENT '璇勮鏁?,
    `collect_count` INT DEFAULT 0 COMMENT '鏀惰棌鏁?,
    `is_top` TINYINT DEFAULT 0 COMMENT '鏄惁缃《',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '鏄惁鍒犻櫎',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='甯栧瓙琛?;

-- =============================================
-- 4. 璇勮琛?-- =============================================
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '璇勮ID',
    `post_id` BIGINT NOT NULL COMMENT '甯栧瓙ID',
    `user_id` BIGINT NOT NULL COMMENT '鐢ㄦ埛ID',
    `content` VARCHAR(500) NOT NULL COMMENT '璇勮鍐呭',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '鏄惁鍒犻櫎',
    PRIMARY KEY (`id`),
    KEY `idx_post_id` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='璇勮琛?;

-- =============================================
-- 5. 鐐硅禐琛?-- =============================================
DROP TABLE IF EXISTS `post_like`;
CREATE TABLE `post_like` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '鐢ㄦ埛ID',
    `post_id` BIGINT NOT NULL COMMENT '甯栧瓙ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_post` (`user_id`, `post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鐐硅禐琛?;

-- =============================================
-- 6. 鏀惰棌琛?-- =============================================
DROP TABLE IF EXISTS `post_collect`;
CREATE TABLE `post_collect` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '鐢ㄦ埛ID',
    `post_id` BIGINT NOT NULL COMMENT '甯栧瓙ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_post` (`user_id`, `post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鏀惰棌琛?;

-- =============================================
-- 7. 瀛︾琛?-- =============================================
DROP TABLE IF EXISTS `subject`;
CREATE TABLE `subject` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '瀛︾ID',
    `name` VARCHAR(50) NOT NULL COMMENT '瀛︾鍚嶇О',
    `sort` INT DEFAULT 0 COMMENT '鎺掑簭',
    `status` TINYINT DEFAULT 1 COMMENT '鐘舵€侊細0绂佺敤 1姝ｅ父',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='瀛︾琛?;

-- =============================================
-- 8. 骞寸骇琛?-- =============================================
DROP TABLE IF EXISTS `grade`;
CREATE TABLE `grade` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '骞寸骇ID',
    `name` VARCHAR(50) NOT NULL COMMENT '骞寸骇鍚嶇О',
    `level` VARCHAR(20) NOT NULL COMMENT '瀛︽',
    `sort` INT DEFAULT 0 COMMENT '鎺掑簭',
    `status` TINYINT DEFAULT 1 COMMENT '鐘舵€侊細0绂佺敤 1姝ｅ父',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='骞寸骇琛?;

-- =============================================
-- 9. 瀛︿範璧勬枡琛?-- =============================================
DROP TABLE IF EXISTS `resource`;
CREATE TABLE `resource` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '璧勬枡ID',
    `name` VARCHAR(200) NOT NULL COMMENT '璧勬枡鍚嶇О',
    `description` TEXT COMMENT '璧勬枡鎻忚堪',
    `file_name` VARCHAR(255) NOT NULL COMMENT '鏂囦欢鍚?,
    `file_path` VARCHAR(500) NOT NULL COMMENT '鏂囦欢璺緞',
    `file_type` VARCHAR(20) NOT NULL COMMENT '鏂囦欢绫诲瀷',
    `file_size` BIGINT NOT NULL COMMENT '鏂囦欢澶у皬',
    `subject_id` BIGINT NOT NULL COMMENT '瀛︾ID',
    `grade_id` BIGINT NOT NULL COMMENT '骞寸骇ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '涓婁紶鑰匢D',
    `download_count` INT DEFAULT 0 COMMENT '涓嬭浇娆℃暟',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '鏄惁鍒犻櫎',
    PRIMARY KEY (`id`),
    KEY `idx_subject_id` (`subject_id`),
    KEY `idx_grade_id` (`grade_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='瀛︿範璧勬枡琛?;

-- =============================================
-- 10. AI瀵硅瘽琛?-- =============================================
DROP TABLE IF EXISTS `chat`;
CREATE TABLE `chat` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `chat_id` VARCHAR(50) NOT NULL COMMENT '瀵硅瘽ID',
    `user_id` BIGINT NOT NULL COMMENT '鐢ㄦ埛ID',
    `title` VARCHAR(200) DEFAULT NULL COMMENT '瀵硅瘽鏍囬',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '鏄惁鍒犻櫎',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_chat_id` (`chat_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI瀵硅瘽琛?;

-- =============================================
-- 11. 瀵硅瘽娑堟伅琛?-- =============================================
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `chat_id` VARCHAR(50) NOT NULL COMMENT '瀵硅瘽ID',
    `role` VARCHAR(20) NOT NULL COMMENT '瑙掕壊锛歶ser/assistant',
    `content` TEXT NOT NULL COMMENT '娑堟伅鍐呭',
    `image` VARCHAR(500) DEFAULT NULL COMMENT '鍥剧墖璺緞',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    PRIMARY KEY (`id`),
    KEY `idx_chat_id` (`chat_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='瀵硅瘽娑堟伅琛?;

-- =============================================
-- 12. 璇剧▼琛?-- =============================================
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '璇剧▼ID',
    `name` VARCHAR(200) NOT NULL COMMENT '璇剧▼鍚嶇О',
    `description` TEXT COMMENT '璇剧▼鎻忚堪',
    `cover_image` VARCHAR(500) DEFAULT NULL COMMENT '灏侀潰鍥?,
    `teacher_name` VARCHAR(50) DEFAULT NULL COMMENT '鏁欏笀鍚嶇О',
    `subject_id` BIGINT NOT NULL COMMENT '瀛︾ID',
    `grade_id` BIGINT NOT NULL COMMENT '骞寸骇ID',
    `chapter_count` INT DEFAULT 0 COMMENT '绔犺妭鏁?,
    `student_count` INT DEFAULT 0 COMMENT '瀛︿範浜烘暟',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '鏄惁鍒犻櫎',
    PRIMARY KEY (`id`),
    KEY `idx_subject_id` (`subject_id`),
    KEY `idx_grade_id` (`grade_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='璇剧▼琛?;

-- =============================================
-- 13. 璇剧▼绔犺妭琛?-- =============================================
DROP TABLE IF EXISTS `course_chapter`;
CREATE TABLE `course_chapter` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '绔犺妭ID',
    `course_id` BIGINT NOT NULL COMMENT '璇剧▼ID',
    `title` VARCHAR(200) NOT NULL COMMENT '绔犺妭鏍囬',
    `sort` INT DEFAULT 0 COMMENT '鎺掑簭',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '鏄惁鍒犻櫎',
    PRIMARY KEY (`id`),
    KEY `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='璇剧▼绔犺妭琛?;

-- =============================================
-- 14. 璇剧▼瑙嗛琛?-- =============================================
DROP TABLE IF EXISTS `course_video`;
CREATE TABLE `course_video` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '瑙嗛ID',
    `course_id` BIGINT NOT NULL COMMENT '璇剧▼ID',
    `chapter_id` BIGINT NOT NULL COMMENT '绔犺妭ID',
    `title` VARCHAR(200) NOT NULL COMMENT '瑙嗛鏍囬',
    `video_url` VARCHAR(500) NOT NULL COMMENT '瑙嗛鍦板潃',
    `duration` INT DEFAULT 0 COMMENT '鏃堕暱(绉?',
    `sort` INT DEFAULT 0 COMMENT '鎺掑簭',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '鏄惁鍒犻櫎',
    PRIMARY KEY (`id`),
    KEY `idx_chapter_id` (`chapter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='璇剧▼瑙嗛琛?;

-- =============================================
-- 15. 瀛︿範杩涘害琛?-- =============================================
DROP TABLE IF EXISTS `learning_progress`;
CREATE TABLE `learning_progress` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '鐢ㄦ埛ID',
    `course_id` BIGINT NOT NULL COMMENT '璇剧▼ID',
    `video_id` BIGINT NOT NULL COMMENT '瑙嗛ID',
    `position` INT DEFAULT 0 COMMENT '鎾斁浣嶇疆(绉?',
    `is_completed` TINYINT DEFAULT 0 COMMENT '鏄惁瀹屾垚',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_video` (`user_id`, `video_id`),
    KEY `idx_user_course` (`user_id`, `course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='瀛︿範杩涘害琛?;

-- =============================================
-- 16. 鍏憡琛?-- =============================================
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '鍏憡ID',
    `title` VARCHAR(200) NOT NULL COMMENT '鍏憡鏍囬',
    `content` TEXT NOT NULL COMMENT '鍏憡鍐呭',
    `status` TINYINT DEFAULT 1 COMMENT '鐘舵€侊細0绂佺敤 1姝ｅ父',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '鏄惁鍒犻櫎',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鍏憡琛?;

-- =============================================
-- 鍒濆鍖栨暟鎹?-- =============================================

-- 绠＄悊鍛樿处鍙凤紙瀵嗙爜锛歛dmin123锛?INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`) VALUES
('admin', '$2a$10$fK5GSp1tp9t4EJVZYzmZeeYO8p7XoLKxi4Y/ZsARHYkgbXWPiD.pO', '绠＄悊鍛?, 1, 1);

-- 瀛︾鏁版嵁
INSERT INTO `subject` (`name`, `sort`, `status`) VALUES
('璇枃', 1, 1),
('鏁板', 2, 1),
('鑻辫', 3, 1),
('鐗╃悊', 4, 1),
('鍖栧', 5, 1),
('鐢熺墿', 6, 1),
('鍘嗗彶', 7, 1),
('鍦扮悊', 8, 1),
('鏀挎不', 9, 1);

-- 骞寸骇鏁版嵁
INSERT INTO `grade` (`name`, `level`, `sort`, `status`) VALUES
('涓€骞寸骇', '灏忓', 1, 1),
('浜屽勾绾?, '灏忓', 2, 1),
('涓夊勾绾?, '灏忓', 3, 1),
('鍥涘勾绾?, '灏忓', 4, 1),
('浜斿勾绾?, '灏忓', 5, 1),
('鍏勾绾?, '灏忓', 6, 1),
('鍒濅竴', '鍒濅腑', 7, 1),
('鍒濅簩', '鍒濅腑', 8, 1),
('鍒濅笁', '鍒濅腑', 9, 1),
('楂樹竴', '楂樹腑', 10, 1),
('楂樹簩', '楂樹腑', 11, 1),
('楂樹笁', '楂樹腑', 12, 1);

-- 甯栧瓙鍒嗙被鏁版嵁
INSERT INTO `category` (`name`, `sort`, `status`) VALUES
('璇枃', 1, 1),
('鏁板', 2, 1),
('鑻辫', 3, 1),
('鐗╃悊', 4, 1),
('鍖栧', 5, 1),
('鐢熺墿', 6, 1),
('鍘嗗彶', 7, 1),
('鍦扮悊', 8, 1),
('鏀挎不', 9, 1),
('缁煎悎', 10, 1);

-- 娴嬭瘯鍏憡
INSERT INTO `announcement` (`title`, `content`, `status`) VALUES
('娆㈣繋浣跨敤浼樼瓟绛旂枒缃?, '浼樼瓟路涓皬瀛︾瓟鐤戠綉姝ｅ紡涓婄嚎鍟︼紒鍦ㄨ繖閲屼綘鍙互鎻愰棶銆佸涔犮€佷氦娴侊紝蹇潵浣撻獙鍚э紒', 1),
('骞冲彴浣跨敤椤荤煡', '璇锋枃鏄庡彂瑷€锛屼簰鐩稿皧閲嶏紝鍏卞悓钀ラ€犺壇濂界殑瀛︿範姘涘洿銆?, 1);

-- notebook and points extension
ALTER TABLE `user`
    ADD COLUMN `phone` VARCHAR(20) DEFAULT NULL COMMENT '鎵嬫満鍙?,
    ADD COLUMN `bio` VARCHAR(255) DEFAULT NULL COMMENT '涓汉绠€浠?,
    ADD COLUMN `points` INT DEFAULT 0 COMMENT '绉垎',
    ADD COLUMN `virtual_balance` INT DEFAULT 0 COMMENT '铏氭嫙甯佷綑棰?;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='閿欓鏈?;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鐢ㄦ埛绛惧埌璁板綍';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='绉垎鏄庣粏';

-- =============================================
-- 鎵╁睍娴嬭瘯鏁版嵁
-- 鎵€鏈夋祴璇曡处鍙烽粯璁ゅ瘑鐮侊細admin123
-- =============================================

-- 琛ュ厖绠＄悊鍛樿祫鏂?UPDATE `user`
SET `phone` = '13800000000',
    `bio` = '骞冲彴榛樿绠＄悊鍛樿处鍙?,
    `points` = 999,
    `virtual_balance` = 999
WHERE `username` = 'admin';

-- 鏅€氭祴璇曠敤鎴?INSERT INTO `user` (`username`, `password`, `nickname`, `avatar`, `phone`, `bio`, `points`, `virtual_balance`, `role`, `status`) VALUES
('student_zhang', '$2a$10$fK5GSp1tp9t4EJVZYzmZeeYO8p7XoLKxi4Y/ZsARHYkgbXWPiD.pO', '寮犲皬闆?, 'https://upload.wikimedia.org/wikipedia/commons/2/26/Portrait_photography.jpg', '13900000001', '鍒濅簩瀛︾敓锛屽枩娆㈡暟瀛﹀拰鐗╃悊', 21, 120, 0, 1),
('student_li', '$2a$10$fK5GSp1tp9t4EJVZYzmZeeYO8p7XoLKxi4Y/ZsARHYkgbXWPiD.pO', '鏉庢槑杞?, 'https://upload.wikimedia.org/wikipedia/commons/1/1e/PORTRAIT_PICTURE.jpg', '13900000002', '楂樹竴瀛︾敓锛屾鍦ㄥ噯澶囪嫳璇綔鏂囨彁鍗?, 21, 88, 0, 1),
('student_wang', '$2a$10$fK5GSp1tp9t4EJVZYzmZeeYO8p7XoLKxi4Y/ZsARHYkgbXWPiD.pO', '鐜嬪彲蹇?, 'https://upload.wikimedia.org/wikipedia/commons/6/6d/Portrait_woman.jpg', '13900000003', '鍒濅笁瀛︾敓锛屽父鏁寸悊鐗╃悊鍜屽寲瀛﹂敊棰?, 11, 64, 0, 1);

-- 娴嬭瘯甯栧瓙
INSERT INTO `post` (`title`, `content`, `user_id`, `category_id`, `view_count`, `like_count`, `comment_count`, `collect_count`, `is_top`, `create_time`, `update_time`) VALUES
('鍒濅簩鏁板锛氫竴娆″嚱鏁版€绘槸鐪嬪浘灏变贡锛屾€庝箞蹇€熷垽鏂鍑忔€э紵', '鎴戠幇鍦ㄤ竴鐪嬪埌涓€娆″嚱鏁板浘鍍忓氨绱у紶锛屽挨鍏舵槸棰樼洰鎶婃枃瀛楁潯浠跺拰鍥惧儚鏀惧湪涓€璧风殑鏃跺€欙紝鎬绘媴蹇冪鍙峰垽鏂敊銆傛湁娌℃湁鍥哄畾姝ラ鍙互鍏堢湅鏂滅巼銆佸啀鐪嬫埅璺濓紝鏈€鍚庡啀鍒ゆ柇澧炲噺鎬э紵', 2, 2, 126, 2, 2, 1, 1, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
('鑻辫浣滄枃鎬诲啓涓嶉暱锛屾€庢牱鎶婂彞瀛愬啓寰楁洿瀹屾暣锛?, '姣忔鍐欒嫳璇綔鏂囬兘鍙湁鍑犲彞绠€鍗曞彞锛岃€佸笀璇村唴瀹瑰お鍗曡杽銆傛垜鎯崇煡閬撳钩鏃跺簲璇ユ€庝箞绉疮杩炴帴璇嶅拰鎵╁啓鍙ュ瀷锛屾墠鑳芥妸涓€娈佃瘽鍐欏畬鏁淬€?, 3, 3, 84, 1, 1, 2, 0, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
('鐗╃悊鍙楀姏鍒嗘瀽鎬绘紡鍔涳紝澶у鏈変粈涔堟鏌ユ柟娉曪紵', '鍋氬彈鍔涘垎鏋愰鏃讹紝鎴戠粡甯告妸鏀寔鍔涘拰鎽╂摝鍔涙紡鎺夛紝灏ゅ叾鏄枩闈㈠拰澶氫釜鐗╀綋鐩歌繛鐨勬椂鍊欍€傛湁娌℃湁涓€濂椾粠鐮旂┒瀵硅薄鍒扮敾鍥炬爣娉ㄧ殑妫€鏌ラ『搴忥紵', 4, 4, 56, 1, 1, 1, 0, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
('骞冲彴绛旂枒鍖哄彂甯栬鑼冭鏄?, '鎻愰棶鏃跺缓璁ˉ鍏呭勾绾с€佸绉戙€侀鐩師鏂囧拰鑷繁鐨勬€濊矾銆傝繖鏍锋洿瀹规槗寰楀埌鍑嗙‘銆佸彲鎵ц鐨勮В绛旓紝涔熸柟渚垮叾浠栧悓瀛︽绱㈢浉浼奸棶棰樸€?, 1, 10, 32, 0, 0, 0, 0, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY));

-- 娴嬭瘯璇勮
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `create_time`) VALUES
(1, 3, '鍙互鍏堢湅鍥惧儚鏄粠宸︿笅鍒板彸涓婅繕鏄粠宸︿笂鍒板彸涓嬶紝鍐嶅垽鏂?k 鐨勬璐燂紝鏈€鍚庡啀缁撳悎棰樼洰缁欏嚭鐨勪氦鐐逛俊鎭€?, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(1, 4, '鎴戣嚜宸变細鍏堝啓涓€鍙ワ細x 澧炲ぇ鏃讹紝y 鏄惁闅忎箣澧炲ぇ銆傛妸鏂囧瓧缈绘垚鍥惧儚璇█鍚庯紝灏变笉瀹规槗涔便€?, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(2, 2, '浣滄枃鍙互鍏堝噯澶囧嚑涓竾鑳借繛鎺ヨ瘝锛屾瘮濡?firstly銆乮n addition銆乤s a result锛屽啀鎶婂師鍥犲拰缁撴灉鍚勮ˉ涓€鍙ャ€?, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(3, 1, '鍙楀姏鍒嗘瀽鏈€鍏抽敭鐨勬槸鍏堥€夌爺绌跺璞★紝鍐嶆妸鎺ヨЕ闈竴涓釜妫€鏌ワ紝寰堝婕忓姏閮芥槸鍥犱负娌℃湁閫愪釜妫€鏌ユ帴瑙﹀叧绯汇€?, DATE_SUB(NOW(), INTERVAL 2 DAY));

-- 娴嬭瘯鐐硅禐
INSERT INTO `post_like` (`user_id`, `post_id`, `create_time`) VALUES
(3, 1, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(4, 1, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(2, 2, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(3, 3, DATE_SUB(NOW(), INTERVAL 2 DAY));

-- 娴嬭瘯鏀惰棌
INSERT INTO `post_collect` (`user_id`, `post_id`, `create_time`) VALUES
(3, 1, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(2, 2, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(4, 2, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 3, DATE_SUB(NOW(), INTERVAL 2 DAY));

-- 娴嬭瘯璧勬枡
INSERT INTO `resource` (`name`, `description`, `file_name`, `file_path`, `file_type`, `file_size`, `subject_id`, `grade_id`, `user_id`, `download_count`, `create_time`) VALUES
('鍒濅腑鏁板鍑芥暟鍩虹鏁寸悊', '閫傚悎鍒濅竴鍒板垵浜屽涔犲嚱鏁板熀纭€姒傚康銆佸浘鍍忎笌甯歌棰樺瀷銆?, 'math-function-summary.txt', 'uploads/resource/demo/math-function-summary.txt', 'txt', 2048, 2, 8, 2, 15, DATE_SUB(NOW(), INTERVAL 6 DAY)),
('楂樹腑鑻辫浣滄枃杩炴帴璇嶆竻鍗?, '鏁寸悊浜嗗父瑙佷綔鏂囪鎺ヨ瘝鍜屽彞寮忔浛鎹㈣〃杈俱€?, 'english-writing-links.txt', 'uploads/resource/demo/english-writing-links.txt', 'txt', 1536, 3, 10, 3, 11, DATE_SUB(NOW(), INTERVAL 5 DAY)),
('鍒濅腑鐗╃悊鍙楀姏鍒嗘瀽妯℃澘', '鍖呭惈鍙楀姏鍒嗘瀽姝ラ銆佸吀鍨嬮敊璇拰鑷娓呭崟銆?, 'physics-force-template.txt', 'uploads/resource/demo/physics-force-template.txt', 'txt', 1890, 4, 9, 4, 9, DATE_SUB(NOW(), INTERVAL 4 DAY));

-- 娴嬭瘯 AI 瀵硅瘽
INSERT INTO `chat` (`chat_id`, `user_id`, `title`, `create_time`, `update_time`) VALUES
('chat_seed_001', 2, '浜屾鍑芥暟姹傛渶鍊?, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
('chat_seed_002', 3, '鑻辫浣滄枃娑﹁壊寤鸿', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT INTO `chat_message` (`chat_id`, `role`, `content`, `create_time`) VALUES
('chat_seed_001', 'user', '鑰佸笀锛岃繖閬撲簩娆″嚱鏁伴鎴戜笉浼氭眰鏈€鍊硷紝鍙互鍏堝憡璇夋垜搴旇鐪嬮《鐐硅繕鏄厤鏂瑰悧锛?, DATE_SUB(NOW(), INTERVAL 2 DAY)),
('chat_seed_001', 'assistant', '鍏堢湅鍑芥暟瑙ｆ瀽寮忔槸鍚︽柟渚跨洿鎺ラ厤鏂癸紱濡傛灉宸茬粡鏄《鐐瑰紡锛屽氨鐩存帴鏍规嵁椤剁偣鍧愭爣鍒ゆ柇鏈€鍊笺€?, DATE_SUB(NOW(), INTERVAL 2 DAY)),
('chat_seed_002', 'user', '璇峰府鎴戞妸杩欐鑻辫浣滄枃寮€澶存敼寰楁洿鑷劧涓€浜涖€?, DATE_SUB(NOW(), INTERVAL 1 DAY)),
('chat_seed_002', 'assistant', '鍙互鍏堢粰鍑轰富棰樺彞锛屽啀琛ュ厖鍘熷洜鍜屼釜浜烘劅鍙楋紝杩欐牱浼氭瘮鍗曠嫭涓€鍙ヨ鐐规洿瀹屾暣銆?, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 娴嬭瘯璇剧▼
INSERT INTO `course` (`name`, `description`, `cover_image`, `teacher_name`, `subject_id`, `grade_id`, `chapter_count`, `student_count`, `create_time`, `update_time`) VALUES
('鍒濅腑鏁板鍑芥暟涓撻」绐佺牬', '鍥寸粫鍑芥暟鍥惧儚銆佽В鏋愬紡鍜屽簲鐢ㄩ寤虹珛瀹屾暣瑙ｉ姝ラ銆?, 'https://upload.wikimedia.org/wikipedia/commons/8/84/Learning_Mathematics.jpg', '鍛ㄨ€佸笀', 2, 8, 2, 2, 0.00, DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
('楂樹腑鑻辫鍐欎綔琛ㄨ揪鎻愬崌', '浠庡彞寮忔墿鍐欍€佽繛鎺ヨ瘝浣跨敤鍒颁綔鏂囩粨鏋勬惌寤虹殑绯荤粺璇剧▼銆?, 'https://upload.wikimedia.org/wikipedia/commons/6/60/A_book.jpg', '闄堣€佸笀', 3, 10, 2, 1, 16.00, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT INTO `course_chapter` (`course_id`, `title`, `sort`, `create_time`) VALUES
(1, '鍑芥暟鍥惧儚涓庡鍑忔€?, 1, DATE_SUB(NOW(), INTERVAL 12 DAY)),
(1, '寰呭畾绯绘暟娉曚笌搴旂敤棰?, 2, DATE_SUB(NOW(), INTERVAL 11 DAY)),
(2, '浣滄枃寮€澶翠笌涓婚鍙?, 1, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(2, '杩炴帴璇嶄笌娈佃惤鎵╁啓', 2, DATE_SUB(NOW(), INTERVAL 9 DAY));

INSERT INTO `course_video` (`course_id`, `chapter_id`, `title`, `video_url`, `duration`, `sort`, `create_time`) VALUES
(1, 1, '鐢ㄥ浘鍍忓垽鏂竴娆″嚱鏁版€ц川', 'https://filesamples.com/samples/video/mp4/sample_640x360.mp4', 13, 1, DATE_SUB(NOW(), INTERVAL 12 DAY)),
(1, 2, '鍑芥暟搴旂敤棰樺父瑙佽闂媶瑙?, 'https://filesamples.com/samples/video/mp4/sample_960x540.mp4', 13, 1, DATE_SUB(NOW(), INTERVAL 11 DAY)),
(2, 3, '鑻辫浣滄枃楂樺垎寮€澶存€庝箞鍐?, 'https://disk.sample.cat/samples/mp4/1416529-sd_640_360_30fps.mp4', 12, 1, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(2, 4, '杩炴帴璇嶈娈佃惤鏇磋繛璐?, 'https://disk.sample.cat/samples/mp4/1416529-sd_960_540_30fps.mp4', 12, 1, DATE_SUB(NOW(), INTERVAL 9 DAY));

INSERT INTO `learning_progress` (`user_id`, `course_id`, `video_id`, `position`, `is_completed`, `create_time`, `update_time`) VALUES
(2, 1, 1, 13, 1, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 1, 2, 7, 0, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 2, 3, 12, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 娴嬭瘯閿欓鏈?INSERT INTO `wrong_question` (`user_id`, `subject_id`, `grade_id`, `question_content`, `my_answer`, `correct_answer`, `error_reason`, `mastery_status`, `create_time`, `update_time`) VALUES
(2, 2, 8, '宸茬煡涓€娆″嚱鏁?y = 2x - 3锛屾眰褰?x = 4 鏃?y 鐨勫€硷紝骞跺垽鏂嚱鏁板鍑忔€с€?, '鎴戠畻鍑?y = 5锛屼絾鏄笉纭畾鍑芥暟鏄笉鏄€掑噺銆?, '褰?x = 4 鏃讹紝y = 5锛涘洜涓?k = 2 > 0锛屾墍浠ュ嚱鏁伴€掑銆?, '鎶婃枩鐜囨璐熷拰澧炲噺鎬у搴斿叧绯昏娣蜂簡銆?, 0, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
(3, 3, 10, '璇峰皢鍙ュ瓙 I like sports. 鎵╁啓鎴愪袱鍙ョ浉鍏炽€佽繛璐殑鑻辫琛ㄨ揪銆?, 'I like sports because it good.', 'I like sports very much. They help me stay healthy and make me feel relaxed.', '璋撹褰㈠紡鍜屽師鍥犲彞琛ㄨ揪涓嶅畬鏁淬€?, 1, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 4, 9, '涓€涓湪鍧楅潤姝㈠湪鏂滈潰涓婏紝璇峰垎鏋愬畠鍙楀埌鐨勫姏銆?, '鍙湁閲嶅姏鍜屾敮鎸佸姏銆?, '鏈ㄥ潡鍙楀埌閲嶅姏銆佹枩闈㈢殑鏀寔鍔涳紝浠ュ強娌挎枩闈㈠悜涓婄殑闈欐懇鎿﹀姏銆?, '蹇界暐浜嗛樆纰嶄笅婊戣秼鍔跨殑鎽╂摝鍔涖€?, 2, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 娴嬭瘯绛惧埌璁板綍
INSERT INTO `user_checkin` (`user_id`, `checkin_date`, `points_reward`, `bonus_points`, `streak_days`, `create_time`) VALUES
(2, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 5, 0, 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 5, 0, 1, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 5, 0, 1, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 娴嬭瘯绉垎鏄庣粏
INSERT INTO `points_record` (`user_id`, `action_type`, `biz_id`, `points`, `remark`, `create_time`) VALUES
(2, 'CHECKIN', DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '%Y-%m-%d'), 5, '姣忔棩绛惧埌', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 'POST_CREATE', '1', 3, '鍙戝竷甯栧瓙', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, 'COMMENT_CREATE', '3', 1, '鍙戣〃璇勮', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 'WRONG_QUESTION_CREATE', '1', 2, '娣诲姞閿欓', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 'CHAPTER_COMPLETE', '1', 10, '瀹屾垚璇剧▼绔犺妭', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 'CHECKIN', DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '%Y-%m-%d'), 5, '姣忔棩绛惧埌', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 'POST_CREATE', '2', 3, '鍙戝竷甯栧瓙', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(3, 'COMMENT_CREATE', '1', 1, '鍙戣〃璇勮', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(3, 'WRONG_QUESTION_CREATE', '2', 2, '娣诲姞閿欓', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 'CHAPTER_COMPLETE', '3', 10, '瀹屾垚璇剧▼绔犺妭', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 'CHECKIN', DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '%Y-%m-%d'), 5, '姣忔棩绛惧埌', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 'POST_CREATE', '3', 3, '鍙戝竷甯栧瓙', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(4, 'COMMENT_CREATE', '2', 1, '鍙戣〃璇勮', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(4, 'WRONG_QUESTION_CREATE', '3', 2, '娣诲姞閿欓', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- paid purchase extension
ALTER TABLE `resource`
    ADD COLUMN `requires_points` TINYINT DEFAULT 0 COMMENT '鏄惁闇€瑕佺Н鍒嗚喘涔?,
    ADD COLUMN `points_cost` INT DEFAULT 0 COMMENT '璐拱鎵€闇€绉垎';

ALTER TABLE `course`
    ADD COLUMN `requires_points` TINYINT DEFAULT 0 COMMENT '鏄惁闇€瑕佽喘涔?,
    ADD COLUMN `points_cost` INT DEFAULT 0 COMMENT '璇剧▼浠锋牸锛堣櫄鎷熷竵锛?;

DROP TABLE IF EXISTS `resource_purchase`;
CREATE TABLE `resource_purchase` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `resource_id` BIGINT NOT NULL,
    `points_cost` INT NOT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_resource` (`user_id`, `resource_id`),
    KEY `idx_resource_purchase_user` (`user_id`),
    KEY `idx_resource_purchase_resource` (`resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='璧勬枡璐拱璁板綍';

DROP TABLE IF EXISTS `course_purchase`;
CREATE TABLE `course_purchase` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_no` VARCHAR(64) NOT NULL COMMENT '璁㈠崟鍙?,
    `user_id` BIGINT NOT NULL,
    `course_id` BIGINT NOT NULL,
    `points_cost` INT NOT NULL COMMENT '璇剧▼浠锋牸锛堣櫄鎷熷竵锛?,
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '璁㈠崟鐘舵€侊細1宸插彂璐?2宸叉敹璐?,
    `deliver_time` DATETIME DEFAULT NULL COMMENT '鍙戣揣鏃堕棿',
    `receive_time` DATETIME DEFAULT NULL COMMENT '鏀惰揣鏃堕棿',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_course` (`user_id`, `course_id`),
    UNIQUE KEY `uk_course_order_no` (`order_no`),
    KEY `idx_course_purchase_user` (`user_id`),
    KEY `idx_course_purchase_course` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='璇剧▼璁㈠崟璁板綍';

-- paid sample data for testing
UPDATE `resource` SET `requires_points` = 1, `points_cost` = 6 WHERE `id` = 2;
UPDATE `resource` SET `requires_points` = 1, `points_cost` = 14 WHERE `id` = 3;
UPDATE `course` SET `requires_points` = 1, `points_cost` = 12 WHERE `id` = 2;

INSERT INTO `resource` (`name`, `description`, `file_name`, `file_path`, `file_type`, `file_size`, `subject_id`, `grade_id`, `user_id`, `download_count`, `requires_points`, `points_cost`, `create_time`) VALUES
('鍒濅腑鏁板鍘嬭酱棰橀敊鍥犳竻鍗曪紙绉垎鐗堬級', '閫傚悎鍒濅簩闃舵澶嶇洏鍑芥暟涓庡嚑浣曞帇杞撮鐨勫父瑙侀敊鍥狅紝璐拱鍚庡彲鐩存帴涓嬭浇銆?, 'math-function-summary.txt', 'uploads/resource/demo/math-function-summary.txt', 'txt', 2048, 2, 8, 2, 0, 1, 9, DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT INTO `course` (`name`, `description`, `cover_image`, `teacher_name`, `subject_id`, `grade_id`, `chapter_count`, `student_count`, `requires_points`, `points_cost`, `create_time`, `update_time`) VALUES
('鍒濅腑鐗╃悊鍙楀姏鍒嗘瀽涓撻」鎻愬崌锛堣櫄鎷熷竵璇撅級', '鍥寸粫鍙楀姏鍒嗘瀽銆佹枩闈㈡懇鎿﹀姏鍜屽鐗╀綋杩炴帴妯″瀷璁捐鐨勮繘闃惰绋嬨€?, 'https://upload.wikimedia.org/wikipedia/commons/4/4e/Physics_classroom.jpg', '鏋楄€佸笀', 4, 9, 1, 0, 1, 16, 16.00, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT INTO `course_chapter` (`course_id`, `title`, `sort`, `create_time`) VALUES
(3, '鍙楀姏鍒嗘瀽杩涢樁妯℃澘', 1, DATE_SUB(NOW(), INTERVAL 2 DAY));

INSERT INTO `course_video` (`course_id`, `chapter_id`, `title`, `video_url`, `duration`, `sort`, `create_time`) VALUES
(3, 5, '涓夋瀹屾垚澶嶆潅鍙楀姏鍒嗘瀽', 'https://filesamples.com/samples/video/mp4/sample_640x360.mp4', 13, 1, DATE_SUB(NOW(), INTERVAL 2 DAY));

-- extra purchase test accounts
INSERT INTO `user` (`username`, `password`, `nickname`, `avatar`, `phone`, `bio`, `points`, `virtual_balance`, `role`, `status`)
SELECT 'points_tester', '$2a$10$fK5GSp1tp9t4EJVZYzmZeeYO8p7XoLKxi4Y/ZsARHYkgbXWPiD.pO', 'Points Tester', 'https://upload.wikimedia.org/wikipedia/commons/2/26/Portrait_photography.jpg', '13900000011', 'Manual purchase testing account', 80, 120, 0, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'points_tester');

INSERT INTO `user` (`username`, `password`, `nickname`, `avatar`, `phone`, `bio`, `points`, `virtual_balance`, `role`, `status`)
SELECT 'points_buyer', '$2a$10$fK5GSp1tp9t4EJVZYzmZeeYO8p7XoLKxi4Y/ZsARHYkgbXWPiD.pO', 'Points Buyer', 'https://upload.wikimedia.org/wikipedia/commons/1/1e/PORTRAIT_PICTURE.jpg', '13900000012', 'Automated purchase verification account', 80, 120, 0, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'points_buyer');

INSERT INTO `points_record` (`user_id`, `action_type`, `biz_id`, `points`, `remark`)
SELECT `id`, 'ADMIN_GRANT', 'seed-points-tester-20260403', 80, '鍒濆鍖栨祴璇曠Н鍒?
FROM `user`
WHERE `username` = 'points_tester'
  AND NOT EXISTS (SELECT 1 FROM `points_record` WHERE `action_type` = 'ADMIN_GRANT' AND `biz_id` = 'seed-points-tester-20260403');

INSERT INTO `points_record` (`user_id`, `action_type`, `biz_id`, `points`, `remark`)
SELECT `id`, 'ADMIN_GRANT', 'seed-points-buyer-20260403', 80, '鍒濆鍖栨祴璇曠Н鍒?
FROM `user`
WHERE `username` = 'points_buyer'
  AND NOT EXISTS (SELECT 1 FROM `points_record` WHERE `action_type` = 'ADMIN_GRANT' AND `biz_id` = 'seed-points-buyer-20260403');

-- normalize all non-admin seeded accounts to password: 123456
UPDATE `user`
SET `password` = '$2a$10$gPu6w6pQ//3Ry6u9AIaExOzNPtlWv3oxGH9gQ8fIOVUjP9n6puWhG'
WHERE `role` <> 1 OR `role` IS NULL;

UPDATE `user`
SET `virtual_balance` = COALESCE(`virtual_balance`, 0);

-- course order migration for RMB pricing
SET @course_price_amount_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'course'
      AND COLUMN_NAME = 'price_amount'
);
SET @course_price_amount_sql := IF(
    @course_price_amount_exists = 0,
    'ALTER TABLE `course` ADD COLUMN `price_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT ''course price in CNY''',
    'SELECT 1'
);
PREPARE stmt FROM @course_price_amount_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @course_purchase_payment_amount_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'course_purchase'
      AND COLUMN_NAME = 'payment_amount'
);
SET @course_purchase_payment_amount_sql := IF(
    @course_purchase_payment_amount_exists = 0,
    'ALTER TABLE `course_purchase` ADD COLUMN `payment_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT ''order amount in CNY''',
    'SELECT 1'
);
PREPARE stmt FROM @course_purchase_payment_amount_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @course_purchase_paid_time_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'course_purchase'
      AND COLUMN_NAME = 'paid_time'
);
SET @course_purchase_paid_time_sql := IF(
    @course_purchase_paid_time_exists = 0,
    'ALTER TABLE `course_purchase` ADD COLUMN `paid_time` DATETIME DEFAULT NULL COMMENT ''payment time''',
    'SELECT 1'
);
PREPARE stmt FROM @course_purchase_paid_time_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @course_purchase_completed_time_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'course_purchase'
      AND COLUMN_NAME = 'completed_time'
);
SET @course_purchase_completed_time_sql := IF(
    @course_purchase_completed_time_exists = 0,
    'ALTER TABLE `course_purchase` ADD COLUMN `completed_time` DATETIME DEFAULT NULL COMMENT ''completion time''',
    'SELECT 1'
);
PREPARE stmt FROM @course_purchase_completed_time_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
UPDATE `course`
SET `price_amount` = CASE
    WHEN `id` = 2 THEN 16.00
    WHEN `requires_points` = 1 AND `price_amount` = 0 THEN `points_cost`
    ELSE `price_amount`
END;

UPDATE `course_purchase` cp
LEFT JOIN `course` c ON c.`id` = cp.`course_id`
SET cp.`payment_amount` = CASE
        WHEN cp.`payment_amount` > 0 THEN cp.`payment_amount`
        WHEN c.`price_amount` > 0 THEN c.`price_amount`
        ELSE cp.`points_cost`
    END,
    cp.`paid_time` = COALESCE(cp.`paid_time`, cp.`deliver_time`),
    cp.`completed_time` = COALESCE(cp.`completed_time`, cp.`receive_time`),
    cp.`status` = CASE
        WHEN cp.`status` = 2 THEN 2
        WHEN cp.`status` = 1 THEN 1
        ELSE 0
    END;
