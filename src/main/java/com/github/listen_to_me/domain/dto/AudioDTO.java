package com.github.listen_to_me.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AudioDTO {
    @NotBlank(message = "标题不能为空")
    private String title;
    @NotBlank(message = "封面不能为空不能为空")
    private String coverUrl;
    private String description;
    @NotBlank(message = "音频不能为空不能为空")
    private String audioUrl;

    private Integer trialDuration;
    private Boolean isPaid;
    private Double price;
    // 可见性（PUBLIC/PRIVATE）
    private String visibility;
}
