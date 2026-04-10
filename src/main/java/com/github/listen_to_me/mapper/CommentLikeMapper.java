package com.github.listen_to_me.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.listen_to_me.domain.entity.CommentLike;

public interface CommentLikeMapper extends BaseMapper<CommentLike> {

    @Select("select comment_id from comment_likes where user_id = #{userId}")
    List<Long> selectLikedIds(Long userId);
}
