package com.github.listen_to_me.domain.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AuditAudioVO {
    private Long id;
    private String title;
    private String coverUrl;
    private Integer duration;
    private Long creatorId;
    private String creatorName;
    private String transcript;
    private LocalDateTime submitTime;
}
