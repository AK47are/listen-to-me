package com.github.listen_to_me.domain.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import com.github.listen_to_me.common.enumeration.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("notification")
@Schema(name = "Notification", description = "站内通知")
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "接收者用户ID")
    private Long recipientId;

    @Schema(description = NotificationType.SCHEMA_DESC)
    private NotificationType type;

    @Schema(description = "通知标题")
    private String title;

    @Schema(description = "通知内容")
    private String content;

    @Schema(description = "关联业务ID（如音频ID、申请ID等）")
    private Long relatedId;

    @Schema(description = "0-未读 1-已读")
    private Boolean isRead;

    private LocalDateTime createTime;
}
