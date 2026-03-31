package com.github.listen_to_me.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "发起预约请求")
public class ConsultDTO {

    @NotNull(message = "时间槽ID不能为空")
    @Schema(description = "时间槽ID", example = "1")
    private Long slotId;

    @NotBlank(message = "留言不能为空")
    @Schema(description = "用户留言", example = "想咨询一下技术问题")
    private String message;
}
