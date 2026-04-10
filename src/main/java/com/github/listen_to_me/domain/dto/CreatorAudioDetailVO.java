package com.github.listen_to_me.domain.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "创作者稿件详情（用于修改音频配置）")
public class CreatorAudioDetailVO {
    private Long id;
    private String title;
    private String coverUrl;
    private String description;
    private Integer trialDuration;
    private Boolean isPaid;
    private BigDecimal price;
    private String visibility;
}
