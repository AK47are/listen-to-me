package com.github.listen_to_me.domain.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import com.github.listen_to_me.common.enumeration.CreatorApplyStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("creator_apply")
public class CreatorApply implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String realName;

    private String phone;

    private String intro;

    private String attachment;

    /**
     * 申请状态
     */
    @Schema(description = CreatorApplyStatus.SCHEMA_DESC)
    private CreatorApplyStatus status;

    /**
     * 审核拒绝原因
     */
    private String reason;

    /**
     * 申请时间
     * 数据库字段：create_time → 实体类字段：applyTime
     */
    @TableField(value = "create_time")
    private LocalDateTime applyTime;
}
