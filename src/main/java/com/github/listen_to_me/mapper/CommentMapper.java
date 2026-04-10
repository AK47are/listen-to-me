package com.github.listen_to_me.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.listen_to_me.domain.entity.Comment;
import com.github.listen_to_me.domain.vo.CommentVO;

public interface CommentMapper extends BaseMapper<Comment> {
    IPage<CommentVO> selectTopPage(Page<CommentVO> page, Long audioId);

    List<CommentVO> selectReplyComments(Long audioId, List<Long> topCommentIds);
}
