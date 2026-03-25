package com.github.listen_to_me.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    private String coverUrl;

    @Schema(description = "MinIO原始路径")
    private String rawPath;

    @Schema(description = "M3U8路径")
    private String hlsPath;

    private BigDecimal price;

    @Schema(description = "试听秒数")
    private Integer trialDuration;

    @Schema(description = "0-待审, 1-通过, 2-违规")
    private Integer auditStatus;

    @Schema(description = "0-草稿/下架, 1-转码中, 2-已发布")
    private Integer status;

    @Schema(description = "点击量/热度基数")
    private Integer viewCount;

    private LocalDateTime createTime;

    @Schema(description = "逻辑删除：0-未删除 1-已删除")
    private Byte isDeleted;

    @Schema(description = "可见性：1-公开可见 0-仅自己/管理员可见")
    private Byte visible;
}
