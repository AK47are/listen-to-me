package com.github.listen_to_me.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.listen_to_me.domain.entity.UserFollow;
import com.github.listen_to_me.domain.vo.CreatorVO;
import com.github.listen_to_me.domain.vo.FansVO;

@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {

    IPage<CreatorVO> selectFollowPage(Page<?> page, Long userId);

    IPage<FansVO> selectFansPage(Page<?> page, Long creatorId);
}
