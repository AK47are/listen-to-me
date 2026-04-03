package com.github.listen_to_me.domain.vo;

import lombok.Data;

@Data
public class AiTaskVO {
    private String taskId;
    private String type; // TRANSCRIPTION, SUMMARIZATION, SLOT_GENERATION
    private String status; // PENDING, PROCESSING, SUCCESS, FAILED
    private Object result; // 应该转成 JSON 对象
    private String failReason;
}
