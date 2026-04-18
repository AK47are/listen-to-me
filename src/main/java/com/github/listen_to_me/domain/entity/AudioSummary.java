package com.github.listen_to_me.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("audio_summary")
public class AudioSummary {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long audioId;
    private String taskId;
    private String summary;
    private LocalDateTime createTime;
}
