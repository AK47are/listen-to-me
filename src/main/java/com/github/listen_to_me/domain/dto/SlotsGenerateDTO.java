package com.github.listen_to_me.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SlotsGenerateDTO {
    @NotBlank(message = "排期描述不能为空")
    @Schema(description = "自然语言描述", example = "每周一三五下午2-4点，单价80元")
    private String description;
}
