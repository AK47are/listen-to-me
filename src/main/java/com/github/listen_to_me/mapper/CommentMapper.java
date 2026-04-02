package com.github.listen_to_me.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.listen_to_me.domain.entity.Comment;
import com.github.listen_to_me.domain.vo.CommentVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CommentMapper extends BaseMapper<Comment> {
    IPage<CommentVO> selectTopPage(Page<CommentVO> page, @Param("audioId") Long audioId);

    List<CommentVO> selectReplyComments(@Param("audioId") Long audioId, @Param("topCommentIds") List<Long> topCommentIds);
}
