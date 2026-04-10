package com.github.listen_to_me.controller.user;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.listen_to_me.common.Result;
import com.github.listen_to_me.domain.dto.CommentDTO;
import com.github.listen_to_me.domain.dto.CommentLikeDTO;
import com.github.listen_to_me.domain.dto.CommentReplyDTO;
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
    public Result<Void> saveComment(@Valid @RequestBody CommentDTO commentDTO) {
        commentService.addComment(commentDTO);
        return Result.success();
    }

    @Operation(summary = "获取评论分页")
    @GetMapping("/page")
    public Result<IPage<CommentVO>> getCommentPage(@ParameterObject CommentQuery commentQuery) {
        return Result.success(commentService.findCommentPage(commentQuery));
    }

    @Operation(summary = "点赞/取消点赞评论")
    @PostMapping("/like")
    public Result<Void> likeComment(@Valid @RequestBody CommentLikeDTO commentLikeDTO) {
        commentLikeService.likeComment(commentLikeDTO);
        return Result.success();
    }

    @Operation(summary = "回复评论")
    @PostMapping("/reply")
    public Result<Void> replyComment(@Valid @RequestBody CommentReplyDTO commentReplyDTO) {
        commentService.replyComment(commentReplyDTO);
        return Result.success();
    }
}
