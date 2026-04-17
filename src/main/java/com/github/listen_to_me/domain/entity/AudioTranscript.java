package com.github.listen_to_me.domain.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;

@Data
public class AudioTranscript implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long audioId;
    private String taskId;
    private String fullText;
    private String segmentJson;
    private LocalDateTime createTime;
}
