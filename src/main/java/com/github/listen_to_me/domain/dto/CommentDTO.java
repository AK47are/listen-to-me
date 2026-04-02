package com.github.listen_to_me.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CommentDTO {
    @NotNull(message = "音频ID不能为空")
    private Long audioId;

    @NotNull(message = "父评论ID不能为空")
    @Min(value = 0, message = "父评论ID不能为负数")
    private Long parentId;

    @NotBlank(message = "评论内容不能为空")
    private String content;
}
