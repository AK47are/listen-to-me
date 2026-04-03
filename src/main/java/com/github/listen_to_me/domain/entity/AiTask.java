package com.github.listen_to_me.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("ai_task")
public class AiTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String taskId;
    private Long userId;
    private Long audioId;
    private String type; // TRANSCRIPTION, SUMMARIZATION, SLOT_GENERATION
    private String status; // PENDING, PROCESSING, SUCCESS, FAILED
    private String result;
    private String failReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
