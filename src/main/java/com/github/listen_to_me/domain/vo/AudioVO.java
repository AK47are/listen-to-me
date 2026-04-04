package com.github.listen_to_me.domain.vo;

import lombok.Data;

@Data
public class AudioVO {
    private Long id;
    private String title;
    private String coverUrl;
    private String creatorName;
    private Long playCount;
    private Long collectCount;
}
