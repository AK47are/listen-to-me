package com.github.listen_to_me.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.listen_to_me.domain.entity.CreatorProfile;
import com.github.listen_to_me.domain.query.CreatorPageQuery;
import com.github.listen_to_me.domain.vo.CreatorVO;

@Mapper
public interface CreatorProfileMapper extends BaseMapper<CreatorProfile> {

    IPage<CreatorVO> selectCreatorPage(Page<?> page, Long userId, CreatorPageQuery query);
}
