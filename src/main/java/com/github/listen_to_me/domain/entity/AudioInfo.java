package com.github.listen_to_me.domain.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import com.github.listen_to_me.common.enumeration.AudioAuditStatus;
import com.github.listen_to_me.common.enumeration.AudioPublishStatus;
import com.github.listen_to_me.common.enumeration.AudioVisibility;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("audio_info")
@Schema(name = "AudioInfo", description = "")
public class AudioInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @Schema(description = "对应 sys_user.id")
    private Long creatorId;
    private String title;
    private String description;
    private String coverPath;
    @Schema(description = "MinIO原始路径")
    private String rawPath;
    private String clipPath;
    private Boolean isPaid;
    private BigDecimal price;
    @Schema(description = "试听秒数")
    private Integer trialDuration;
    private Integer duration;
    @Schema(description = AudioAuditStatus.SCHEMA_DESC)
    private AudioAuditStatus auditStatus;
    @Schema(description = AudioPublishStatus.SCHEMA_DESC)
    private AudioPublishStatus status;
    @Schema(description = "点击量/热度基数")
    private Integer playCount;
    @Schema(description = "点赞数")
    private Integer likeCount;
    @Schema(description = "收藏数")
    private Integer collectCount;
    @Schema(description = "评论数")
    private Integer commentCount;
    private LocalDateTime createTime;
    @Schema(description = "逻辑删除：0-未删除 1-已删除")
    @TableLogic
    private Byte isDeleted;
    @Schema(description = AudioVisibility.SCHEMA_DESC)
    private AudioVisibility visibility;
    private String rejectReason;
}
