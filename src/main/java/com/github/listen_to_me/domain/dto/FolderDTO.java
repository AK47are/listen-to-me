package com.github.listen_to_me.domain.dto;

import lombok.Data;

@Data
public class FolderDTO {
    private String name; // 收藏夹名称（必填）

    private String description; // 描述（可选）
}
