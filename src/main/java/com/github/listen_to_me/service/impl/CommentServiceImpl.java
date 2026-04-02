package com.github.listen_to_me.service.impl;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.houbb.sensitive.word.core.SensitiveWordHelper;
import com.github.listen_to_me.common.exception.BaseException;
import com.github.listen_to_me.common.util.MinioUtils;
import com.github.listen_to_me.common.util.SecurityUtils;
import com.github.listen_to_me.domain.dto.CommentDTO;
import com.github.listen_to_me.domain.dto.CommentReplyDTO;
import com.github.listen_to_me.domain.entity.Comment;
import com.github.listen_to_me.domain.entity.CommentLike;
import com.github.listen_to_me.domain.query.CommentQuery;
import com.github.listen_to_me.domain.vo.CommentVO;
import com.github.listen_to_me.mapper.AudioInfoMapper;
import com.github.listen_to_me.mapper.CommentLikeMapper;
import com.github.listen_to_me.mapper.CommentMapper;
import com.github.listen_to_me.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
    private final CommentMapper commentMapper;
    private final AudioInfoMapper audioInfoMapper;
    private final CommentLikeMapper commentLikeMapper;

    @Override
    public void addComment(CommentDTO commentDTO) {
        Long currId = SecurityUtils.getCurrentUserId();
        if (audioInfoMapper.selectById(commentDTO.getAudioId()) == null) {
            throw new BaseException(404,"音频不存在");
        }
        if (commentDTO.getParentId() != 0 &&
                commentMapper.selectById(commentDTO.getParentId()) == null) {
            throw new BaseException(404,"父评论不存在");
        }
        if (SensitiveWordHelper.contains(commentDTO.getContent())) {
            throw new BaseException(400,"评论内容包含敏感词");
        }
        Comment comment = new Comment();
        comment.setUserId(currId);
        comment.setAudioId(commentDTO.getAudioId());
        comment.setParentId(commentDTO.getParentId());
        comment.setContent(commentDTO.getContent());
        commentMapper.insert(comment);
    }

    @Override
    public IPage<CommentVO> findCommentPage(CommentQuery commentQuery) {
        Long currId = SecurityUtils.getCurrentUserId();
        // 1. 查询所有顶级评论
        Page<CommentVO> topPage = new Page<>(commentQuery.getPageNum(), commentQuery.getPageSize());
        IPage<CommentVO> topPageResult = commentMapper.selectTopPage(topPage, commentQuery.getAudioId());
        List<CommentVO> topComments = topPageResult.getRecords();
        // 2. 查询所有回复评论
        List<Long> topCommentIds = topPageResult.getRecords().stream()
                .map(CommentVO::getId)
                .toList();
        List<CommentVO> replyComments = commentMapper.selectReplyComments(commentQuery.getAudioId(),topCommentIds);
        // 3. 查询当前用户的点赞ids, 并设置点赞状态
        List<Long> likedCommentIds = commentLikeMapper.selectLikedIds(currId);
        topComments.forEach(commentVO -> {
            commentVO.setLikedByCurrentUser(likedCommentIds.contains(commentVO.getId()));
            commentVO.setAvatar(MinioUtils.getPresignedUrl(commentVO.getAvatar()));
        });
        replyComments.forEach(commentVO -> {
            commentVO.setLikedByCurrentUser(likedCommentIds.contains(commentVO.getId()));
            commentVO.setAvatar(MinioUtils.getPresignedUrl(commentVO.getAvatar()));
        });
        // 4. 构建 Map<id,commentVO> 映射
        Map<Long, CommentVO> commentMap = new HashMap<>();
        for (CommentVO vo : topComments) commentMap.put(vo.getId(), vo);
        for (CommentVO vo : replyComments) commentMap.put(vo.getId(), vo);
        // 5. 构建回复评论树
        for (CommentVO vo : commentMap.values()) {
            Long parentId = vo.getParentId();
            if (parentId > 0 && commentMap.containsKey(parentId)) {
                commentMap.get(parentId).getReplyList().add(vo);
            }
        }
        return topPageResult;
    }

    @Override
    public void replyComment(CommentReplyDTO commentReplyDTO) {
        Long currId = SecurityUtils.getCurrentUserId();
        Comment replayComment = commentMapper.selectById(commentReplyDTO.getCommentId());
        if (replayComment == null) {
            throw new BaseException(404,"评论不存在");
        }
        if (commentReplyDTO.getContent().isBlank()) {
            throw new BaseException(400,"回复内容不能为空");
        }
        if(SensitiveWordHelper.contains(commentReplyDTO.getContent())){
            throw new BaseException(400,"回复内容包含敏感词");
        }
        Comment comment = new Comment();
        comment.setUserId(currId);
        comment.setAudioId(replayComment.getAudioId());
        comment.setParentId(commentReplyDTO.getCommentId());
        comment.setContent(commentReplyDTO.getContent());
        commentMapper.insert(comment);
    }
}
