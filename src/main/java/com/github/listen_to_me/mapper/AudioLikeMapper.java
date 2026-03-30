package com.github.listen_to_me.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.listen_to_me.domain.entity.AudioInfo;
import com.github.listen_to_me.domain.entity.AudioLike;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface AudioLikeMapper extends BaseMapper<AudioLike> {
    @Select({
            "SELECT ai.* ",
            "FROM audio_info ai ",
            "INNER JOIN audio_like al ON ai.id = al.audio_id ",
            "WHERE al.user_id = #{userId}"
    })
    IPage<AudioInfo> selectUserLikeAudioList(Page<AudioInfo> page, @Param("userId") Long userId);
}
