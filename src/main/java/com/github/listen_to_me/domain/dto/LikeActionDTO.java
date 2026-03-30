package com.github.listen_to_me.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LikeActionDTO {
    @Schema(description = "音频id")
    private Long audioId;
    @Schema(description = "操作类型, LIKE（喜欢）/ UNLIKE（取消喜欢）")
    private String action;
}
