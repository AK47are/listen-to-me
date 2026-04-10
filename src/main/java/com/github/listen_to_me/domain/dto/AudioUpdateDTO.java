package com.github.listen_to_me.domain.dto;

import org.jetbrains.annotations.NotNull;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AudioUpdateDTO {
    @NotNull
    private Long id;

    @NotBlank(message = "标题不能为空白")
    private String title;

    @Pattern(regexp = "^(http|https)://.*$", message = "封面URL格式无效")
    private String coverUrl;
    private String description;
    @Min(value = 0, message = "试听时长不能为负数")
    private Integer trialDuration;
    private Boolean isPaid;
    @Positive(message = "收费音频价格必须大于0")
    private Double price;
    @Pattern(regexp = "^(PUBLIC|PRIVATE)$", message = "可见性参数无效，仅支持 PUBLIC 或 PRIVATE")
    private String visibility;
}
