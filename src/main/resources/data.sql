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
('个人中心', 'user:info'),
('封面上传', 'cover:upload'),
('用户基本权限','user:basic'),
('创作者基本权限','creator:basic'),
('管理员基本权限','admin:basic');

-- 系统用户（密码均为 123456）
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `avatar`, `phone`, `email`, `openid`, `is_creator`, `balance`, `frozen_balance`, `status`) VALUES
('admin', '$2a$10$jUcTKqj1NZ5KZFqIW9hba.7RSiXLd2GxVBxQm6KI7QO40XedjJdU2', '系统管理员', 'img/avatar/默认头像.png', '13800138000', 'admin@example.com', 'wx_admin_001', 0, 9999999.00, 0.00, 'NORMAL'),
('creator_01', '$2a$10$jUcTKqj1NZ5KZFqIW9hba.7RSiXLd2GxVBxQm6KI7QO40XedjJdU2', '有声的小雅', 'img/avatar/默认头像.png', '13800138001', 'wx_creator_001@example.com','wx_creator_001', 1, 936.38, 320.50, 'NORMAL'),
('listener_01', '$2a$10$jUcTKqj1NZ5KZFqIW9hba.7RSiXLd2GxVBxQm6KI7QO40XedjJdU2', '听书小迷弟', 'img/avatar/默认头像.png', '13800138002', 'wx_listener_001@example.com', 'wx_listener_001', 0, 44.30, 0.00, 'NORMAL'),
('listener_02', '$2a$10$jUcTKqj1NZ5KZFqIW9hba.7RSiXLd2GxVBxQm6KI7QO40XedjJdU2', '深夜听众', 'img/avatar/默认头像.png', '13800138003', 'wx_listener_002@example.com', 'wx_listener_002', 0, 67.20, 0.00, 'NORMAL'),
('creator_02', '$2a$10$jUcTKqj1NZ5KZFqIW9hba.7RSiXLd2GxVBxQm6KI7QO40XedjJdU2', '老杨说故事', 'img/avatar/默认头像.png', '13800138004', 'wx_creator_002@example.com', 'wx_creator_002@example', 1, 734.10, 156.20, 'NORMAL');

-- 用户-角色关联
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1, 1),  -- 管理员关联超级管理员角色
(2, 2),  -- 小雅关联创作者角色
(5, 2),  -- 老杨关联创作者角色
(3, 3),  -- 小迷弟关联普通听众角色
(4, 3);  -- 深夜听众关联普通听众角色

-- 角色-权限关联
INSERT INTO `sys_role_permission` (`role_id`, `perm_id`) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), -- 管理员拥有所有权限
(2, 1), (2, 2), (2, 5), (2, 6),  (2, 7), (2, 8),                -- 创作者拥有上传、删除、个人中心、封面上传权限
(3, 5), (3, 7);                                  -- 普通听众仅拥有个人中心权限

-- ----------------------------
-- 2. 音频资产域 - 测试数据
-- ----------------------------
-- 音频信息
INSERT INTO `audio_info` (`creator_id`, `title`,`description`, `cover_path`, `raw_path`, `clip_path`, `is_paid`, `price`, `trial_duration`, `duration`, `audit_status`, `play_count`) VALUES
(2, '心理学入门30讲', '这是一个心理学入门的课程，包含心理学的定义、研究对象、认知心理学入门等', 'online/cover/20260401/987df0b1-4254-4618-aa8a-51a2b9410dcb_b_fd2f1f7cc2e6138a9de0a078385c2b81.jpg', 'online/audio/20260401/9231c36e-28a9-431c-8726-5881f4707d33_周杰伦 - 兰亭序.mp3', 'online/audio/clip/20260401/clip_9_5152756370235071512.mp3', 1, 29.90, 60, 60, 1, 2),
(2, '职场沟通技巧', '这是一个职场沟通技巧的课程，包含沟通的核心、有效倾听、职场表达的3个技巧等', 'online/cover/20260401/987df0b1-4254-4618-aa8a-51a2b9410dcb_b_fd2f1f7cc2e6138a9de0a078385c2b81.jpg', 'online/audio/20260401/9231c36e-28a9-431c-8726-5881f4707d33_周杰伦 - 兰亭序.mp3', 'online/audio/clip/20260401/clip_9_5152756370235071512.mp3', 1, 19.90, 45, 240, 1, 2),
(5, '民间故事大全', '这是一个民间故事的课程，包含民间的狐仙故事、民间的狼妖传说等', 'online/cover/20260401/987df0b1-4254-4618-aa8a-51a2b9410dcb_b_fd2f1f7cc2e6138a9de0a078385c2b81.jpg', 'online/audio/20260401/9231c36e-28a9-431c-8726-5881f4707d33_周杰伦 - 兰亭序.mp3', 'online/audio/clip/20260401/clip_9_5152756370235071512.mp3',1, 9.90, 30, 300, 1, 2),
(5, '悬疑短篇合集', '这是一个悬疑短篇的课程，包含悬疑的狐仙故事、悬疑的狼妖传说等', 'online/cover/20260401/987df0b1-4254-4618-aa8a-51a2b9410dcb_b_fd2f1f7cc2e6138a9de0a078385c2b81.jpg', 'online/audio/20260401/9231c36e-28a9-431c-8726-5881f4707d33_周杰伦 - 兰亭序.mp3', 'online/audio/clip/20260401/clip_9_5152756370235071512.mp3', 1, 15.90, 40, 480, 1, 2),
(2, '负能量清理指南', '这是一个负能量清理指南的课程，包含负能量的定义、清理的方法、清理的注意事项等', 'online/cover/20260401/987df0b1-4254-4618-aa8a-51a2b9410dcb_b_fd2f1f7cc2e6138a9de0a078385c2b81.jpg', 'online/audio/20260401/9231c36e-28a9-431c-8726-5881f4707d33_周杰伦 - 兰亭序.mp3', 'online/audio/clip/20260401/clip_9_5152756370235071512.mp3', 1, 12.90, 30, 300, 1, 2);

-- 音频转写
INSERT INTO `audio_transcript` (`audio_id`, `full_text`, `segment_json`) VALUES
(1, '大家好，今天我们开始心理学入门的第一讲，首先来了解什么是心理学...（全文省略）', '[{"time":0,"title":"第1讲：心理学的定义"},{"time":300,"title":"第1讲：心理学的研究对象"},{"time":600,"title":"第2讲：认知心理学入门"}]'),
(2, '职场沟通中，倾听是最重要的环节，很多人沟通失败的原因是不会听...（全文省略）', '[{"time":0,"title":"沟通的核心：有效倾听"},{"time":240,"title":"职场表达的3个技巧"}]'),
(3, '今天给大家讲一个民间的狐仙故事，这个故事发生在民国时期的北方小镇...（全文省略）', '[{"time":0,"title":"民国狐仙故事"},{"time":480,"title":"山村狼妖传说"}]');

-- ----------------------------
-- 3. 交易业务域 - 测试数据
-- ----------------------------
-- 订单信息（订单编号格式：YYYYMMDD+6位随机数）
INSERT INTO `audio_order` (`order_sn`, `user_id`, `audio_id`, `pay_amount`, `pay_status`, `pay_channel`, `pay_time`) VALUES
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
INSERT INTO `consult_slot` (`creator_id`, `start_time`, `end_time`, `price`, `address`, `status`) VALUES
(2, '2025-05-25 10:00:00', '2025-05-25 11:00:00', 50.00, '腾讯会议链接：https://meeting.tencent.com/dm/abc123', 'BOOKED'),
(2, '2025-05-25 14:00:00', '2025-05-25 15:00:00', 60.00, '腾讯会议链接：https://meeting.tencent.com/dm/def456', 'AVAILABLE'),
(2, '2025-05-26 09:00:00', '2025-05-26 10:00:00', 50.00, '腾讯会议链接：https://meeting.tencent.com/dm/ghi789', 'AVAILABLE'),
(5, '2025-05-26 14:00:00', '2025-05-26 15:30:00', 80.00, '腾讯会议链接：https://meeting.tencent.com/dm/jkl012', 'AVAILABLE'),
(5, '2025-05-27 19:00:00', '2025-05-27 20:00:00', 70.00, '腾讯会议链接：https://meeting.tencent.com/dm/mno345', 'CANCELLED'),
(2, '2025-05-28 16:00:00', '2025-05-28 17:00:00', 55.00, '腾讯会议链接：https://meeting.tencent.com/dm/pqr678', 'EXPIRED'),
(5, '2025-05-29 20:00:00', '2025-05-29 21:30:00', 90.00, '腾讯会议链接：https://meeting.tencent.com/dm/stu901', 'AVAILABLE');

-- ----------------------------
-- 5. 咨询服务域 - 测试数据（预约订单）
-- ----------------------------
-- 预约订单
INSERT INTO `consult_order` (`slot_id`, `user_id`, `creator_id`, `message`, `status`, `address`, `pay_amount`, `create_time`) VALUES
(1, 3, 2, '想咨询一下心理学入门相关的问题', 'CONFIRMED', '腾讯会议链接：https://meeting.tencent.com/dm/abc123', 50.00, '2025-05-20 10:00:00'),
(2, 3, 2, '请问职场沟通技巧有哪些', 'PENDING_CONFIRM', NULL, 60.00, '2025-05-21 14:30:00'),
(3, 4, 2, '想了解心理学入门课程', 'PENDING_CONFIRM', NULL, 50.00, '2025-05-22 09:15:00'),
(4, 4, 5, '民间故事创作经验分享', 'CONFIRMED', '腾讯会议链接：https://meeting.tencent.com/dm/jkl012', 80.00, '2025-05-23 19:00:00'),
(5, 3, 5, '悬疑故事创作技巧', 'CANCELLED', NULL, 70.00, '2025-05-24 11:00:00');

-- 退款申请
INSERT INTO `refund_apply` (`order_id`, `user_id`, `reason`, `status`, `reject_reason`, `create_time`) VALUES
(1, 3, '咨询内容与预期不符', 'PROCESSED', NULL, '2025-05-25 10:00:00'),
(4, 4, '创作者未按时回复', 'PENDING', NULL, '2025-05-26 14:00:00');

-- ----------------------------
-- 6. 收藏夹域 - 测试数据
-- ----------------------------
-- 用户3（听书小迷弟）的收藏夹
INSERT INTO `folder` (`id`, `name`, `description`, `audio_count`) VALUES
(1, '我的最爱', '超级喜欢的音频合集', 3),
(2, '心理学专区', '心理学相关学习音频', 2),
(3, '睡前故事', '睡前听的放松故事', 1);

-- 用户4（深夜听众）的收藏夹
INSERT INTO `folder` (`id`, `name`, `description`, `audio_count`) VALUES
(4, '我的最爱', '超级喜欢的音频合集', 2),
(5, '睡前故事', '睡前听的放松故事', 1);

-- 用户 - 收藏夹关联
INSERT INTO `sys_user_folder` (`user_id`, `folder_id`) VALUES
(3, 1),
(3, 2),
(3, 3),
(4, 4),
(4, 5);

-- 音频 - 收藏夹关联（用户3的收藏夹）
INSERT INTO `audio_folder_relation` (`audio_id`, `folder_id`) VALUES
(1, 1),
(1, 2),
(2, 1),
(2, 2),
(3, 1),
(3, 3);

-- 音频 - 收藏夹关联（用户4的收藏夹）
INSERT INTO `audio_folder_relation` (`audio_id`, `folder_id`) VALUES
(1, 4),
(3, 5);

-- ----------------------------
-- 7. 虚拟币流水域 - 测试数据
-- ----------------------------
INSERT INTO `coin_transaction` (`user_id`, `type`, `biz_type`, `amount`, `balance_before`, `balance_after`, `biz_id`, `remark`) VALUES
(1, 'INCOME', 'RECHARGE', 9999999.00, 0.00, 9999999.00, 'RC_ADMIN_001', '管理员初始充值'),
-- 创作者 2 (有声的小雅)
(2, 'INCOME', 'RECHARGE', 1256.88, 0.00, 1256.88, 'RC202503240001', '初始充值'),
(2, 'EXPENSE', 'REFUND', 320.50, 1256.88, 936.38, 'REF202503240001', '退款'),
-- 创作者 5 (老杨说故事)
(5, 'INCOME', 'RECHARGE', 890.30, 0.00, 890.30, 'RC202503240002', '初始充值'),
(5, 'EXPENSE', 'REFUND', 156.20, 890.30, 734.10, 'REF202503240002', '退款'),
-- 用户 3 (听书小迷弟)
(3, 'INCOME', 'RECHARGE', 100.00, 0.00, 100.00, 'RC202503240003', '初始充值'),
(3, 'EXPENSE', 'AUDIO', 29.90, 100.00, 70.10, '20250520123456', '购买音频：心理学入门30讲'),
(3, 'EXPENSE', 'AUDIO', 9.90, 70.10, 60.20, '20250521654321', '购买音频：民间故事大全'),
(3, 'EXPENSE', 'AUDIO', 15.90, 60.20, 44.30, '20250524789456', '购买音频：悬疑短篇合集'),
-- 用户 4 (深夜听众)
(4, 'INCOME', 'RECHARGE', 100.00, 0.00, 100.00, 'RC202503240004', '初始充值'),
(4, 'EXPENSE', 'AUDIO', 19.90, 100.00, 80.10, '20250522987654', '购买音频：职场沟通技巧'),
(4, 'EXPENSE', 'AUDIO', 12.90, 80.10, 67.20, '20250523456789', '购买音频：负能量清理指南');


-- -------------------------------------
-- 评论表测试数据
-- -------------------------------------
INSERT INTO comments (id,audio_id, parent_id, user_id, content, like_count) VALUES
-- 1. 音频1 顶级评论 (parent_id=0)
(1,1, 0, 1, '音频1的第一条顶级评论，支持一下！', 2),
(2,1, 0, 2, '这个音频内容很棒', 1),
-- 2. 音频1 二级评论 (回复id=1)
(3,1, 1, 3, '回复：我也觉得不错', 1),
-- 3. 音频1 三级评论 (回复id=3，A←B←C 层级)
(4,1, 3, 4, '回复二楼：确实经典', 0),
-- 4. 音频2 顶级评论
(5,2, 0, 5, '音频2打卡学习', 3),
(6,2, 0, 1, '支持主播更新', 2),
-- 5. 音频2 二级评论 (回复id=5)
(7,2, 5, 2, '一起学习！', 1),
-- 6. 音频2 三级评论 (回复id=7)
(8,2, 7, 3, '打卡+1', 0),
-- 7. 音频3 顶级评论
(9,3, 0, 4, '音频3干货满满', 1),
(10,3, 0, 5, '收藏了', 1),
-- 8. 音频3 二级评论
(11,3, 9, 1, '学到了', 0),
-- 9. 音频4 顶级评论
(12,4, 0, 2, '音频4强烈推荐', 2),
-- 10. 音频5 顶级评论
(13,5, 0, 3, '音频5非常专业', 1),
(14,5, 0, 4, '已三连', 1),
(15,5, 0, 5, '坐等更新', 0),
-- 11. 音频4 三级评论 (回复id=12)
(16,2, 8, 1,'支持',0);

-- 评论点赞表批量插入（与comments.like_count精准匹配，共18条点赞记录）
INSERT INTO comment_likes (comment_id, user_id) VALUES
-- 评论1 (like_count=2)：用户2、3点赞
(1, 2), (1, 3),
-- 评论2 (like_count=1)：用户1点赞
(2, 1),
-- 评论3 (like_count=1)：用户5点赞
(3, 5),
-- 评论5 (like_count=3)：用户1、3、4点赞
(5, 1), (5, 3), (5, 4),
-- 评论6 (like_count=2)：用户2、5点赞
(6, 2), (6, 5),
-- 评论7 (like_count=1)：用户4点赞
(7, 4),
-- 评论9 (like_count=1)：用户2点赞
(9, 2),
-- 评论10 (like_count=1)：用户3点赞
(10, 3),
-- 评论12 (like_count=2)：用户1、5点赞
(12, 1), (12, 5),
-- 评论13 (like_count=1)：用户1点赞
(13, 1),
-- 评论14 (like_count=1)：用户2点赞
(14, 2);
-- 开启外键检查


-- 创作者申请测试数据
-- 批量插入创作者申请记录（已通过审核）
INSERT INTO creator_apply (user_id, real_name, phone, status) VALUES
(2, '陈梓菱', '13800138001', 'APPROVED'),
(5, '张宏', '13800138004', 'APPROVED');

-- 用户关注创作者记录
INSERT INTO user_follow (user_id, creator_id, create_time) VALUES
-- 听书小迷弟 (user_id=3) 关注了 有声的小雅 (creator_id=2)
(3, 2, '2025-05-20 10:30:00'),
-- 听书小迷弟 (user_id=3) 关注了 老杨说故事 (creator_id=5)
(3, 5, '2025-05-21 14:20:00'),
-- 深夜听众 (user_id=4) 关注了 有声的小雅 (creator_id=2)
(4, 2, '2025-05-22 09:15:00');

INSERT INTO `creator_profile` (`user_id`, `intro`) VALUES
(2, '10年后端开发经验，擅长微服务架构设计、分布式系统、高并发处理。曾任职于多家互联网大厂。'),
(5, '12年产品经验，从0到1主导过多个千万级用户产品，擅长产品规划和用户增长策略。');

SET FOREIGN_KEY_CHECKS = 1;
