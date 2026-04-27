package com.github.listen_to_me.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.listen_to_me.domain.entity.Comment;
import com.github.listen_to_me.domain.vo.CommentVO;

public interface CommentMapper extends BaseMapper<Comment> {
    @Update("UPDATE comments SET like_count = like_count + #{delta} WHERE id = #{commentId}")
    void incrementLikeCount(Long commentId, int delta);

    IPage<CommentVO> selectTopPage(Page<CommentVO> page, Long audioId);

    List<CommentVO> selectReplyComments(Long audioId, List<Long> topCommentIds);
}
