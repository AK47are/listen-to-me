package com.github.listen_to_me.controller.user;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.listen_to_me.common.Result;
import com.github.listen_to_me.domain.dto.CommentDTO;
import com.github.listen_to_me.domain.dto.CommentLikeDTO;
import com.github.listen_to_me.domain.query.CommentQuery;
import com.github.listen_to_me.domain.vo.CommentVO;
import com.github.listen_to_me.service.CommentLikeService;
import com.github.listen_to_me.service.CommentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "评论接口")
@RequiredArgsConstructor
@RestController
@RequestMapping("/user/comment")
public class CommentController {
    private final CommentService commentService;
    private final CommentLikeService commentLikeService;

    @PostMapping
    @Operation(summary = "发布评论")
    public Result<Void> saveComment(@AuthenticationPrincipal Long userId, @Valid @RequestBody CommentDTO commentDTO) {
        commentService.addComment(userId, commentDTO);
        return Result.success();
    }

    @Operation(summary = "获取评论分页")
    @GetMapping("/page")
    public Result<IPage<CommentVO>> getCommentPage(@AuthenticationPrincipal Long userId,
            @Valid @ParameterObject CommentQuery commentQuery) {
        return Result.success(commentService.findCommentPage(userId, commentQuery));
    }

    @Operation(summary = "点赞/取消点赞评论")
    @PostMapping("/like")
    public Result<Void> likeComment(@AuthenticationPrincipal Long userId,
            @Valid @RequestBody CommentLikeDTO commentLikeDTO) {
        commentLikeService.likeComment(userId, commentLikeDTO);
        return Result.success();
    }
}
