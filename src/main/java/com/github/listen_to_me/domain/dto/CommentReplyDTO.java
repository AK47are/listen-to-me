package com.github.listen_to_me.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentReplyDTO {
    @NotNull(message = "评论ID不能为空")
    private Long commentId;
    @NotBlank(message = "回复内容不能为空")
    private String content;
    @Schema(description = "回复的用户ID")
    private Long replyToUserId;
}
