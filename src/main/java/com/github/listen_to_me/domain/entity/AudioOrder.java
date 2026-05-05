package com.github.listen_to_me.domain.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import com.github.listen_to_me.common.enumeration.PayChannel;
import com.github.listen_to_me.common.enumeration.PayStatus;
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
@TableName("audio_order")
@Schema(name = "AudioOrder", description = "")
public class AudioOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String orderSn;

    private Long userId;

    private Long audioId;

    private BigDecimal payAmount;

    @Schema(description = PayStatus.SCHEMA_DESC)
    private PayStatus payStatus;

    @Schema(description = PayChannel.SCHEMA_DESC)
    private PayChannel payChannel;

    private LocalDateTime payTime;

    private LocalDateTime createTime;
}
