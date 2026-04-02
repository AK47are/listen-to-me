package com.github.listen_to_me.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentLikeDTO {
    @NotNull(message = "评论ID不能为空")
    private Long commentId;

    @Schema(description = "操作类型 LIKE/UNLIKE")
    @NotBlank(message = "操作类型不能为空")
    private String action;
}
