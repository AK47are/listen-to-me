package com.github.listen_to_me.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.listen_to_me.domain.vo.AudioVO;

@Mapper
public interface AudioVOMapper {

    IPage<AudioVO> selectByFolderId(Page<AudioVO> page, Long folderId);

}
