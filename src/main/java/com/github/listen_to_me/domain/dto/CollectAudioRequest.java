package com.github.listen_to_me.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CollectAudioRequest {
    @Schema(description = "收藏夹ID（可选，不传则使用默认收藏夹）")
    private Long folderId;
}
