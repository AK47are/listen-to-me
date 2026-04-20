CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `username` varchar(64) UNIQUE NOT NULL COMMENT '账号',
  `password` varchar(128) NOT NULL COMMENT 'BCrypt加密',
  `nickname` varchar(64) COMMENT '昵称',
  `avatar` varchar(500) DEFAULT 'img/avatar/默认头像.png' COMMENT '头像地址',
  `phone` varchar(20) UNIQUE COMMENT '手机号',
  `email` varchar(128) UNIQUE COMMENT '邮箱',
  `openid` varchar(128) COMMENT '三方平台唯一标识',
  `is_creator` tinyint(1) DEFAULT 0 COMMENT '0-听众, 1-创作者',
  `balance` decimal(12, 2) DEFAULT 0.00 COMMENT '可提现余额',
  `frozen_balance` decimal(12, 2) DEFAULT 0.00 COMMENT '账期内冻结金额',
  `status` enum('NORMAL','BANNED') DEFAULT 'NORMAL' COMMENT '状态: NORMAL(正常), BANNED(封禁)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_email (email),
  INDEX idx_phone (phone),
  INDEX idx_openid (openid),
  INDEX idx_is_creator (is_creator),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `sys_role` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `role_name` varchar(32) NOT NULL,
  `role_code` varchar(32) NOT NULL COMMENT 'ROLE_ADMIN, ROLE_CREATOR',

 UNIQUE INDEX uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `sys_permission` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `perm_name` varchar(64) NOT NULL,
  `perm_code` varchar(64) NOT NULL COMMENT 'audio:upload, audio:delete',

  UNIQUE INDEX uk_perm_code (perm_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 用户-角色, 角色-权限
CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `user_id` bigint, `role_id` bigint, PRIMARY KEY (`user_id`, `role_id`),
  CONSTRAINT fk_user_role_user FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT fk_user_role_role FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`)

);
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
  `role_id` bigint, `perm_id` bigint, PRIMARY KEY (`role_id`, `perm_id`),
  INDEX idx_perm_id (perm_id),
  CONSTRAINT fk_role_perm_role FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`),
  CONSTRAINT fk_role_perm_perm FOREIGN KEY (`perm_id`) REFERENCES `sys_permission` (`id`)
);

CREATE TABLE IF NOT EXISTS `audio_info` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `creator_id` bigint NOT NULL COMMENT '对应 sys_user.id',
  `title` varchar(255) NOT NULL,
  `description` varchar(255) COMMENT '描述',
  `cover_path` varchar(500),
  `raw_path` varchar(500) COMMENT 'MinIO原始路径',
  `clip_path` varchar(500) COMMENT 'MinIO剪切路径',
  `is_paid` tinyint DEFAULT 0 COMMENT '0-免费 1-付费',
  `price` decimal(10, 2) DEFAULT 0.00,
  `duration` int DEFAULT 0 COMMENT '总时长',
  `trial_duration` int DEFAULT 0 COMMENT '试听秒数',
  `audit_status` ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING' COMMENT '审核状态：PENDING-待审核, APPROVED-已通过, REJECTED-已拒绝',
  `reject_reason` varchar(255) DEFAULT NULL COMMENT '拒绝原因',
  `status` ENUM('PENDING_TRANSCODE', 'TRANSCODING', 'ONLINE', 'FAILED') NOT NULL DEFAULT 'PENDING_TRANSCODE' COMMENT '发布状态: 待转码, 转码中, 已上线, 转码失败',
  `play_count` int DEFAULT 0 COMMENT '播放量',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `visibility` enum('PUBLIC', 'PRIVATE') DEFAULT 'PUBLIC' COMMENT '可见性：PUBLIC-公开可见 PRIVATE-仅自己/管理员可见',
  INDEX idx_visibility (visibility),
  INDEX idx_is_deleted (is_deleted),
  INDEX idx_creator_id (creator_id),
  INDEX idx_audit_status (audit_status),
  INDEX idx_status (status),
  INDEX idx_price (price),
  CONSTRAINT fk_audio_creator FOREIGN KEY (`creator_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `audio_transcript` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `audio_id` bigint NOT NULL,
  `task_id` varchar(64) COMMENT '关联的AI任务ID',
  `full_text` longtext COMMENT 'AI转写文本',
  `segment_json` json COMMENT '完整转写结果（sentences + words + emotion）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  UNIQUE INDEX uk_audio_id (audio_id),
  INDEX idx_task_id (task_id),
  CONSTRAINT fk_transcript_audio FOREIGN KEY (`audio_id`) REFERENCES `audio_info` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='音频转写结果表';

CREATE TABLE IF NOT EXISTS `audio_order` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `order_sn` varchar(64) UNIQUE NOT NULL,
  `user_id` bigint NOT NULL,
  `audio_id` bigint NOT NULL,
  `pay_amount` decimal(10, 2) NOT NULL,
  `pay_status` int DEFAULT 0 COMMENT '0-待支付, 1-已支付, 2-已取消',
  `pay_channel` varchar(20) COMMENT 'alipay, wechat',
  `pay_time` datetime,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,

  INDEX idx_user_id (user_id),
  INDEX idx_audio_id (audio_id),
  INDEX idx_order_sn (order_sn),
  INDEX idx_pay_status (pay_status),
  CONSTRAINT fk_order_user FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT fk_order_audio FOREIGN KEY (`audio_id`) REFERENCES `audio_info` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `play_history` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint,
  `audio_id` bigint,
  `last_position` int DEFAULT 0 COMMENT '秒数进度',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE INDEX uk_user_audio (user_id, audio_id),
  INDEX idx_audio_id (audio_id),
  CONSTRAINT fk_play_history_user FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT fk_play_history_audio FOREIGN KEY (`audio_id`) REFERENCES `audio_info` (`id`)
);

CREATE TABLE IF NOT EXISTS `consult_slot` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `creator_id` bigint NOT NULL COMMENT '创作者ID, 关联 sys_user.id',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `price` decimal(10, 2) NOT NULL COMMENT '预约价格（虚拟币）',
  `address` varchar(500) NOT NULL COMMENT '预约地址（如腾讯会议链接）',
  `status` varchar(20) DEFAULT 'AVAILABLE' COMMENT '状态: AVAILABLE, BOOKED, EXPIRED, CANCELLED',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_creator_id (creator_id),
  INDEX idx_status (status),
  INDEX idx_start_time (start_time),
  CONSTRAINT fk_consult_creator FOREIGN KEY (`creator_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='时间槽表';

CREATE TABLE IF NOT EXISTS `consult_order` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `slot_id` bigint NOT NULL COMMENT '时间槽ID',
  `user_id` bigint NOT NULL COMMENT '预约用户ID',
  `creator_id` bigint NOT NULL COMMENT '创作者ID',
  `message` varchar(500) NOT NULL COMMENT '用户留言',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING_CONFIRM' COMMENT '订单状态: PENDING_CONFIRM, CONFIRMED, COMPLETED, CANCELLED, REFUND_PENDING, REFUNDED',
  `address` varchar(500) COMMENT '预约地址（确认时填充）',
  `pay_amount` decimal(10, 2) NOT NULL COMMENT '支付金额',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_slot_id (slot_id),
  INDEX idx_user_id (user_id),
  INDEX idx_creator_id (creator_id),
  INDEX idx_status (status),
  CONSTRAINT fk_consult_order_slot FOREIGN KEY (`slot_id`) REFERENCES `consult_slot` (`id`),
  CONSTRAINT fk_consult_order_user FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT fk_consult_order_creator FOREIGN KEY (`creator_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约订单表';

CREATE TABLE IF NOT EXISTS `refund_apply` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '预约订单ID',
  `user_id` bigint NOT NULL COMMENT '申请用户ID',
  `reason` varchar(500) NOT NULL COMMENT '退款原因',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '申请状态: PENDING, PROCESSED',
  `reject_reason` varchar(500) COMMENT '拒绝原因',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '处理时间',
  INDEX idx_order_id (order_id),
  INDEX idx_user_id (user_id),
  INDEX idx_status (status),
  CONSTRAINT fk_refund_apply_order FOREIGN KEY (`order_id`) REFERENCES `consult_order` (`id`),
  CONSTRAINT fk_refund_apply_user FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款申请表';

-- 收藏夹表
CREATE TABLE IF NOT EXISTS `folder` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏夹ID',
  `user_id` bigint NOT NULL COMMENT '所属用户ID',
  `name` varchar(50) NOT NULL COMMENT '收藏夹名称',
  `description` varchar(255) DEFAULT NULL COMMENT '收藏夹描述',
  `audio_count` bigint DEFAULT '0' COMMENT '音频数量',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_folder_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏夹表';

-- 音频收藏夹关联表
CREATE TABLE IF NOT EXISTS `audio_folder_relation` (
  `audio_id` bigint NOT NULL COMMENT '音频ID',
  `folder_id` bigint NOT NULL COMMENT '收藏夹ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`audio_id`,`folder_id`) USING BTREE COMMENT '联合主键',
  INDEX `idx_folder_id` (`folder_id`),
  INDEX `idx_create_time` (`create_time`),
  CONSTRAINT `fk_relation_folder` FOREIGN KEY (`folder_id`) REFERENCES `folder` (`id`),
  CONSTRAINT `fk_relation_audio` FOREIGN KEY (`audio_id`) REFERENCES `audio_info` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='音频-收藏夹关联表';

-- 用户喜欢表
CREATE TABLE  IF NOT EXISTS `audio_like` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `audio_id` bigint NOT NULL COMMENT '音频ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`user_id`,`audio_id`) COMMENT '联合主键',
  KEY `fk_audio_like_audio` (`audio_id`),
  CONSTRAINT `fk_audio_like_audio` FOREIGN KEY (`audio_id`) REFERENCES `audio_info` (`id`),
  CONSTRAINT `fk_audio_like_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='音频点赞表';

-- 虚拟币流水表
CREATE TABLE IF NOT EXISTS `coin_transaction` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `type` varchar(20) NOT NULL COMMENT '交易类型: EXPENSE(支出), INCOME(收入)',
  `amount` decimal(12, 2) NOT NULL COMMENT '变动金额（正数）',
  `balance_before` decimal(12, 2) NOT NULL COMMENT '变动前余额',
  `balance_after` decimal(12, 2) NOT NULL COMMENT '变动后余额',
  `biz_id` varchar(64) NOT NULL COMMENT '业务ID（订单号/充值单号）',
  `biz_type` varchar(20) NOT NULL COMMENT '业务类型: AUDIO(音频购买), CONSULT(咨询预约), RECHARGE(充值), REFUND(退款)',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_user_id (user_id),
  INDEX idx_biz_id (biz_id),
  INDEX idx_create_time (create_time),
  CONSTRAINT fk_coin_transaction_user FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='虚拟币流水表';


-- 评论表
CREATE TABLE IF NOT EXISTS comments (
  id bigint PRIMARY KEY AUTO_INCREMENT,
  audio_id bigint NOT NULL,      -- 评论所属对象ID
  parent_id bigint DEFAULT 0 COMMENT '父评论ID，0 表示顶评论',       -- 父评论ID，0 表示顶评论
  user_id bigint NOT NULL,
  content text NOT NULL,
  like_count bigint DEFAULT 0 COMMENT '点赞数',
  create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_audio_id_created (audio_id, create_time),
  INDEX idx_parent_id (parent_id),
  INDEX idx_user_id (user_id),
  CONSTRAINT fk_comments_user FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT fk_comments_audio FOREIGN KEY (`audio_id`) REFERENCES `audio_info` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

CREATE TABLE IF NOT EXISTS comment_likes (
  id bigint PRIMARY KEY AUTO_INCREMENT,
  comment_id bigint NOT NULL COMMENT '被点赞的评论ID',
  user_id bigint NOT NULL COMMENT '点赞用户ID',
  create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  INDEX idx_comment_id (comment_id),
  INDEX idx_user_id (user_id),
  UNIQUE KEY uk_comment_user (comment_id, user_id) COMMENT '防止同一用户重复点赞同一评论',
  CONSTRAINT fk_likes_comment FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE,
  CONSTRAINT fk_likes_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论点赞表';

-- 创作者申请表
CREATE TABLE IF NOT EXISTS creator_apply (
  id bigint PRIMARY KEY AUTO_INCREMENT,
  user_id bigint NOT NULL,                           -- 申请人用户ID
  real_name varchar(50) NOT NULL COMMENT '真实姓名',   -- 真实姓名
  phone varchar(11) NOT NULL COMMENT '联系电话',     -- 手机号码
  intro text COMMENT '个人简介/申请说明',            -- 自我介绍/申请理由
  attachment varchar(500) COMMENT '申请附件',         -- 资质附件地址/文件信息
  status ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING' COMMENT '申请状态：待审核/已通过/已拒绝', -- 申请状态
  reason varchar(255) COMMENT '审核拒绝原因',        -- 审核不通过的原因
  create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间', -- 申请提交时间
  UNIQUE INDEX idx_user_id (user_id),                       -- 用户ID索引
  INDEX idx_status (status),                         -- 申请状态索引
  INDEX idx_create_time (create_time),                   -- 申请时间索引
  CONSTRAINT fk_creator_apply_user FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='创作者申请表';

-- 用户关注创作者表
CREATE TABLE IF NOT EXISTS `user_follow` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '关注者用户ID',
  `creator_id` bigint NOT NULL COMMENT '被关注的创作者ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  UNIQUE INDEX uk_user_creator (user_id, creator_id),
  INDEX idx_creator_id (creator_id),
  INDEX idx_user_id (user_id),
  CONSTRAINT fk_follow_user FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT fk_follow_creator FOREIGN KEY (`creator_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注创作者表';

-- 创作者资料表
CREATE TABLE IF NOT EXISTS `creator_profile` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint NOT NULL UNIQUE COMMENT '用户ID',
  `intro` text COMMENT '个人简介',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_creator_profile_user FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='创作者资料表';

-- 用户虚拟币充值订单表
CREATE TABLE IF NOT EXISTS `user_recharge_order` (
    `id` bigint PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `recharge_sn` varchar(64) UNIQUE NOT NULL COMMENT '充值订单号（RC开头）',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `recharge_amount` decimal(10, 2) NOT NULL COMMENT '充值金额（元）',
    `pay_status` ENUM('PENDING', 'PAID', 'CANCELLED') NOT NULL DEFAULT 'PENDING' COMMENT '支付状态：PENDING-待支付，PAID-已支付，CANCELLED-已取消',
    `pay_channel` varchar(20) COMMENT '支付渠道：alipay-支付宝，wechat-微信',
    `pay_time` datetime COMMENT '支付成功时间',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX idx_user_id (user_id),
    INDEX idx_recharge_sn (recharge_sn),
    INDEX idx_pay_status (pay_status),

    CONSTRAINT fk_recharge_user FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户虚拟币充值订单表';

CREATE TABLE IF NOT EXISTS `ai_task` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `task_id` varchar(64) UNIQUE NOT NULL COMMENT '任务ID',
  `user_id` bigint NOT NULL COMMENT '创建者ID',
  `audio_id` bigint DEFAULT NULL COMMENT '音频ID（转写/摘要时必填）',
  `type` ENUM('TRANSCRIPTION', 'SUMMARIZATION', 'SLOT_GENERATION') NOT NULL COMMENT '任务类型',
  `status` ENUM('PENDING', 'PROCESSING', 'SUCCESS', 'FAILED') NOT NULL DEFAULT 'PENDING' COMMENT '任务状态',
  `result` json COMMENT '任务结果',
  `fail_reason` varchar(500) COMMENT '失败原因',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_task_id (task_id),
  INDEX idx_user_id (user_id),
  INDEX idx_audio_id (audio_id),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI任务表';

-- 音频摘要结果表
CREATE TABLE IF NOT EXISTS `audio_summary` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `audio_id` bigint NOT NULL COMMENT '音频ID',
  `task_id` varchar(64) COMMENT '关联的AI任务ID',
  `summary` text NOT NULL COMMENT 'AI生成的中文摘要',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  UNIQUE INDEX uk_audio_id (audio_id),
  INDEX idx_task_id (task_id),
  CONSTRAINT fk_summary_audio FOREIGN KEY (`audio_id`) REFERENCES `audio_info` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='音频摘要结果表';
