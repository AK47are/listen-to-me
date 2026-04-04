package com.github.listen_to_me.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.listen_to_me.common.exception.BaseException;
import com.github.listen_to_me.common.util.SecurityUtils;
import com.github.listen_to_me.domain.dto.CommentLikeDTO;
import com.github.listen_to_me.domain.entity.Comment;
import com.github.listen_to_me.domain.entity.CommentLike;
import com.github.listen_to_me.mapper.CommentLikeMapper;
import com.github.listen_to_me.mapper.CommentMapper;
import com.github.listen_to_me.service.CommentLikeService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CommentLikeServiceImpl extends ServiceImpl<CommentLikeMapper, CommentLike> implements CommentLikeService {
    private final CommentMapper commentMapper;
    private final CommentLikeMapper commentLikeMapper;

    @Override
    @Transactional
    public void likeComment(CommentLikeDTO commentLikeDTO) {
        Long currId = SecurityUtils.getCurrentUserId();
        Comment comment = commentMapper.selectById(commentLikeDTO.getCommentId());
        if (comment == null) {
            throw new BaseException(404, "评论不存在");
        }
        if ("LIKE".equals(commentLikeDTO.getAction())) {
            CommentLike commentLike = new CommentLike();
            commentLike.setCommentId(commentLikeDTO.getCommentId());
            commentLike.setUserId(currId);
            this.save(commentLike);
            commentMapper.update(Wrappers.lambdaUpdate(Comment.class)
                    .set(Comment::getLikeCount, comment.getLikeCount() + 1)
                    .eq(Comment::getId, comment.getId()));
        } else if ("UNLIKE".equals(commentLikeDTO.getAction())) {
            CommentLike commentLike = commentLikeMapper.selectOne(Wrappers.lambdaQuery(CommentLike.class)
                    .eq(CommentLike::getCommentId, commentLikeDTO.getCommentId())
                    .eq(CommentLike::getUserId, currId));
            if (commentLike == null) {
                throw new BaseException(400, "未点赞该评论");
            }
            commentLikeMapper.delete(Wrappers.lambdaQuery(CommentLike.class)
                    .eq(CommentLike::getCommentId, commentLikeDTO.getCommentId())
                    .eq(CommentLike::getUserId, currId));
            commentMapper.update(Wrappers.lambdaUpdate(Comment.class)
                    .set(Comment::getLikeCount, comment.getLikeCount() - 1)
                    .eq(Comment::getId, comment.getId()));
        } else {
            throw new BaseException(400, "操作类型错误");
        }
    }
}
