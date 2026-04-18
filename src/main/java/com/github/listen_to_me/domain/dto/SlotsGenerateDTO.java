package com.github.listen_to_me.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SlotsGenerateDTO {
    @Schema(description = "自然语言描述", example = "每周一三五下午2-4点，单价80元")
    private String description;
}
