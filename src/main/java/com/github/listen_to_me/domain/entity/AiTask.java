package com.github.listen_to_me.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import com.github.listen_to_me.common.enumeration.AiTaskStatus;
import com.github.listen_to_me.common.enumeration.AiTaskType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@TableName("ai_task")
@Schema(description = "AI任务")
public class AiTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String taskId;
    private Long userId;
    private Long audioId;
    @Schema(description = AiTaskType.SCHEMA_DESC)
    private AiTaskType type;
    @Schema(description = AiTaskStatus.SCHEMA_DESC)
    private AiTaskStatus status;
    private String result;
    private String failReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
