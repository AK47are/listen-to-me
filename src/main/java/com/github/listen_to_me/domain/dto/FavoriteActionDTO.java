package com.github.listen_to_me.domain.dto;

import lombok.Data;

@Data
public class FavoriteActionDTO {
    private Long audioId; // 音频ID（必填）
    private Long folderId; // 收藏夹ID（可选，不传则收藏至默认收藏夹）
    private String action; // COLLECT（收藏）/ UNCOLLECT（取消收藏）

}
