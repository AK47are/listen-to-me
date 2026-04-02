package com.github.listen_to_me.controller.user;

import com.github.listen_to_me.common.Result;
import com.github.listen_to_me.domain.dto.CommentDTO;
import com.github.listen_to_me.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "评论接口")
@RequiredArgsConstructor
@RestController
@RequestMapping("/user/comment")
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    @Operation(summary = "发布评论")
    public Result<Void> saveComment(@Valid @RequestBody CommentDTO commentDTO) {
        commentService.addComment(commentDTO);
        return Result.success();
    }
}
