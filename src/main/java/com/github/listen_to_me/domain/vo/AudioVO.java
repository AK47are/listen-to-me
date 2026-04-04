package com.github.listen_to_me.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AudioVO {

    private String title;

    private String coverUrl;

    private BigDecimal price;

    private Integer trialDuration;

    private Integer auditStatus;

    private Integer status;

    private Integer viewCount;

    private LocalDateTime createTime;

    private Byte isDeleted;

    private Byte visible;
}
