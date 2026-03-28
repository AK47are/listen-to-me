package com.github.listen_to_me.domain.dto;

import lombok.Data;

@Data
public class FavoriteDeleteDTO {
    private Long audioId;
    private Long folderId;
}
