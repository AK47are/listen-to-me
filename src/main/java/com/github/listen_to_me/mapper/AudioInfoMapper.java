package com.github.listen_to_me.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.listen_to_me.domain.entity.AudioInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.listen_to_me.domain.query.AudioQuery;
import com.github.listen_to_me.domain.vo.AudioVO;
import io.lettuce.core.dynamic.annotation.Param;

public interface AudioInfoMapper extends BaseMapper<AudioInfo> {
    IPage<AudioVO> selectAudioPage(Page<AudioVO> page, @Param("query") AudioQuery query);
}
