package com.github.listen_to_me.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author kun
 * @since 2026-03-24
 */
@Getter
@Setter
@TableName("audio_info")
public class AudioInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 对应 sys_user.id
     */
    private Long creatorId;

    private String title;

    private String coverUrl;

    /**
     * MinIO原始路径
     */
    private String rawPath;

    /**
     * M3U8路径
     */
    private String hlsPath;

    private BigDecimal price;

    /**
     * 试听秒数
     */
    private Integer trialDuration;

    /**
     * 0-待审, 1-通过, 2-违规
     */
    private Integer auditStatus;

    /**
     * 0-草稿/下架, 1-转码中, 2-已发布
     */
    private Integer status;

    /**
     * 点击量/热度基数
     */
    private Integer viewCount;

    private LocalDateTime createTime;
}
