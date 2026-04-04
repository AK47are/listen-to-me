package com.github.listen_to_me.domain.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class FolderVO {
    private Long id;
    private String name;
    private String description;
    private Long audioCount;
    private LocalDateTime createTime;
}
