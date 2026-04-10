package com.github.listen_to_me.domain.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author baomidou
 * @since 2026-03-25
 */
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
    @Schema(description = "0-待审, 1-通过, 2-违规")
    private Integer auditStatus;
    @Schema(description = "发布状态: PENDING_TRANSCODE(待转码), TRANSCODING(转码中), ONLINE(已上线), FAILED(转码失败)")
    private String status;
    @Schema(description = "点击量/热度基数")
    private Integer playCount;
    private LocalDateTime createTime;
    @Schema(description = "逻辑删除：0-未删除 1-已删除")
    @TableLogic
    private Byte isDeleted;
    @Schema(description = "可见性：PUBLIC-公开可见 PRIVATE-仅自己/管理员可见")
    private String visibility;
    private String rejectReason;
}
