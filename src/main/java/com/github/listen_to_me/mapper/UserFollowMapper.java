package com.github.listen_to_me.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.listen_to_me.domain.entity.UserFollow;

@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {
}
