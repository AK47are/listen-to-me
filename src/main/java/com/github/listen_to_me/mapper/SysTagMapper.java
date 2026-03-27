package com.github.listen_to_me.mapper;

import com.github.listen_to_me.domain.entity.SysTag;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.listen_to_me.domain.vo.AudioTagVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysTagMapper extends BaseMapper<SysTag> {
    List<AudioTagVO> selectTagsByAudioIds(@Param("audioIds") List<Long> audioIds);
}
