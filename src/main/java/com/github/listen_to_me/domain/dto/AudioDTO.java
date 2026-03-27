package com.github.listen_to_me.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AudioDTO {
    private Long creatorId;

    private String title;

    private String coverUrl;

    private String rawPath;

    private String hlsPath;

    private BigDecimal price;

    private Integer trialDuration;

    private Byte visible;

    private List<Long> tagIds;

}
