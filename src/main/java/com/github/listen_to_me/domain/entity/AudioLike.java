package com.github.listen_to_me.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("audio_like")
@Schema(name = "AudioLike", description = "音频喜欢关联")
public class AudioLike {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.INPUT)
    private Long userId;
    private Long audioId;
}
