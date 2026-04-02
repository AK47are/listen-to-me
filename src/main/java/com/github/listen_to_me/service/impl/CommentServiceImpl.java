package com.github.listen_to_me.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.houbb.sensitive.word.core.SensitiveWordHelper;
import com.github.listen_to_me.common.exception.BaseException;
import com.github.listen_to_me.common.util.SecurityUtils;
import com.github.listen_to_me.domain.dto.CommentDTO;
import com.github.listen_to_me.domain.entity.Comment;
import com.github.listen_to_me.mapper.AudioInfoMapper;
import com.github.listen_to_me.mapper.CommentMapper;
import com.github.listen_to_me.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
    private final CommentMapper commentMapper;
    private final AudioInfoMapper audioInfoMapper;

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
}
