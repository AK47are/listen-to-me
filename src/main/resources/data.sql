-- 关闭外键检查（避免导入顺序问题，执行完后开启）
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. RBAC权限域 - 测试数据
-- ----------------------------
-- 系统角色
INSERT INTO `sys_role` (`role_name`, `role_code`) VALUES
('超级管理员', 'ROLE_ADMIN'),
('音频创作者', 'ROLE_CREATOR'),
('普通听众', 'ROLE_USER');

-- 系统权限
INSERT INTO `sys_permission` (`perm_name`, `perm_code`) VALUES
('音频上传', 'audio:upload'),
('音频删除', 'audio:delete'),
('音频审核', 'audio:audit'),
('订单管理', 'order:manage'),
('个人中心', 'user:info');

-- 系统用户（密码均为123456的BCrypt加密值：$10$gFUGfVORKKodIm65H.HEwu1637/tVrFhYgmMQVlYEX40EcUvAoFzi）
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `avatar`, `phone`, `openid`, `is_creator`, `balance`, `frozen_balance`, `version`) VALUES
('admin', '$10$gFUGfVORKKodIm65H.HEwu1637/tVrFhYgmMQVlYEX40EcUvAoFzi', '系统管理员', 'https://avatar.com/admin.jpg', '13800138000', 'wx_admin_001', 0, 0.00, 0.00, 0),
('creator_01', '$10$gFUGfVORKKodIm65H.HEwu1637/tVrFhYgmMQVlYEX40EcUvAoFzi', '有声的小雅', 'https://avatar.com/creator01.jpg', '13800138001', 'wx_creator_001', 1, 1256.88, 320.50, 0),
('listener_01', '$10$gFUGfVORKKodIm65H.HEwu1637/tVrFhYgmMQVlYEX40EcUvAoFzi', '听书小迷弟', 'https://avatar.com/listener01.jpg', '13800138002', 'wx_listener_001', 0, 89.60, 0.00, 0),
('listener_02', '$10$gFUGfVORKKodIm65H.HEwu1637/tVrFhYgmMQVlYEX40EcUvAoFzi', '深夜听众', 'https://avatar.com/listener02.jpg', '13800138003', 'wx_listener_002', 0, 56.20, 0.00, 0),
('creator_02', '$10$gFUGfVORKKodIm65H.HEwu1637/tVrFhYgmMQVlYEX40EcUvAoFzi', '老杨说故事', 'https://avatar.com/creator02.jpg', '13800138004', 'wx_creator_002', 1, 890.30, 156.20, 0);

-- 用户-角色关联
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1, 1),  -- 管理员关联超级管理员角色
(2, 2),  -- 小雅关联创作者角色
(5, 2),  -- 老杨关联创作者角色
(3, 3),  -- 小迷弟关联普通听众角色
(4, 3);  -- 深夜听众关联普通听众角色

-- 角色-权限关联
INSERT INTO `sys_role_permission` (`role_id`, `perm_id`) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5),  -- 管理员拥有所有权限
(2, 1), (2, 2), (2, 5),                  -- 创作者拥有上传、删除、个人中心权限
(3, 5);                                  -- 普通听众仅拥有个人中心权限

-- ----------------------------
-- 2. 音频资产域 - 测试数据
-- ----------------------------
-- 音频信息
INSERT INTO `audio_info` (`creator_id`, `title`, `cover_url`, `raw_path`, `hls_path`, `price`, `trial_duration`, `audit_status`, `status`, `view_count`) VALUES
(2, '心理学入门30讲', 'https://cover.com/psy30.jpg', 'minio/audio/psy30_raw.mp3', 'minio/audio/psy30_hls.m3u8', 29.90, 60, 1, 2, 1568),
(2, '职场沟通技巧', 'https://cover.com/office_talk.jpg', 'minio/audio/office_raw.mp3', 'minio/audio/office_hls.m3u8', 19.90, 45, 1, 2, 892),
(5, '民间故事大全', 'https://cover.com/story.jpg', 'minio/audio/story_raw.mp3', 'minio/audio/story_hls.m3u8', 9.90, 30, 1, 2, 2356),
(5, '悬疑短篇合集', 'https://cover.com/suspense.jpg', 'minio/audio/suspense_raw.mp3', NULL, 15.90, 40, 0, 1, 328),
(2, '负能量清理指南', 'https://cover.com/negative.jpg', 'minio/audio/negative_raw.mp3', NULL, 12.90, 30, 2, 0, 105);

-- 音频转写
INSERT INTO `audio_transcript` (`audio_id`, `full_text`, `segment_json`) VALUES
(1, '大家好，今天我们开始心理学入门的第一讲，首先来了解什么是心理学...（全文省略）', '[{"time":0,"title":"第1讲：心理学的定义"},{"time":300,"title":"第1讲：心理学的研究对象"},{"time":600,"title":"第2讲：认知心理学入门"}]'),
(2, '职场沟通中，倾听是最重要的环节，很多人沟通失败的原因是不会听...（全文省略）', '[{"time":0,"title":"沟通的核心：有效倾听"},{"time":240,"title":"职场表达的3个技巧"}]'),
(3, '今天给大家讲一个民间的狐仙故事，这个故事发生在民国时期的北方小镇...（全文省略）', '[{"time":0,"title":"民国狐仙故事"},{"time":480,"title":"山村狼妖传说"}]');

-- ----------------------------
-- 3. 交易业务域 - 测试数据
-- ----------------------------
-- 订单信息（订单编号格式：YYYYMMDD+6位随机数）
INSERT INTO `order_info` (`order_sn`, `user_id`, `audio_id`, `pay_amount`, `pay_status`, `pay_channel`, `pay_time`) VALUES
('20250520123456', 3, 1, 29.90, 1, 'wechat', '2025-05-20 14:30:25'),
('20250521654321', 3, 3, 9.90, 1, 'alipay', '2025-05-21 09:15:40'),
('20250522987654', 4, 2, 19.90, 1, 'wechat', '2025-05-22 20:05:10'),
('20250523456789', 4, 5, 12.90, 2, 'alipay', NULL),
('20250524789456', 3, 4, 15.90, 0, 'wechat', NULL);

-- ----------------------------
-- 4. 社交与标签域 - 测试数据
-- ----------------------------
-- 系统标签
INSERT INTO `sys_tag` (`name`) VALUES
('心理学'), ('职场'), ('民间故事'), ('悬疑'), ('自我提升'), ('情感');

-- 音频-标签关联
INSERT INTO `audio_tag_relation` (`audio_id`, `tag_id`) VALUES
(1, 1), (1, 5),
(2, 2), (2, 5),
(3, 3),
(4, 3), (4, 4),
(5, 5), (5, 6);

-- 播放历史
INSERT INTO `play_history` (`user_id`, `audio_id`, `last_position`) VALUES
(3, 1, 1250),
(3, 3, 890),
(4, 2, 560),
(4, 1, 320),
(3, 4, 180);

-- ----------------------------
-- 5. 咨询服务域 - 测试数据
-- ----------------------------
-- 咨询时段
INSERT INTO `consult_slot` (`creator_id`, `start_time`, `end_time`, `status`) VALUES
(2, '2025-05-25 10:00:00', '2025-05-25 11:00:00', 0),
(2, '2025-05-25 14:00:00', '2025-05-25 15:00:00', 2),
(5, '2025-05-26 09:00:00', '2025-05-26 10:00:00', 1),
(5, '2025-05-26 19:00:00', '2025-05-26 20:00:00', 0),
(2, '2025-05-27 16:00:00', '2025-05-27 17:00:00', 3);

-- 开启外键检查
SET FOREIGN_KEY_CHECKS = 1;