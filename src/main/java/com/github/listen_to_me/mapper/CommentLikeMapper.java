package com.github.listen_to_me.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.listen_to_me.domain.entity.CommentLike;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CommentLikeMapper extends BaseMapper<CommentLike> {
    @Select("select comment_id from comment_likes where user_id = #{userId}")
    List<Long> selectLikedIds(@Param("userId") Long userId);
}
