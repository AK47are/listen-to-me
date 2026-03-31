package com.github.listen_to_me.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.listen_to_me.domain.entity.AudioInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author kun
 * @since 2026-03-24
 */
public interface AudioInfoMapper extends BaseMapper<AudioInfo> {

    IPage<AudioInfo> selectAudioByFolderId(Page<AudioInfo> page, @Param("folderId") Long folderId);

    void updateStatusById(Long audioId, String status);

    void updateStatusAndClipPathById(Long audioId, String status, String clipPath);
    @Select("SELECT a.* FROM audio_info a WHERE  a.creator_id = #{userId} AND a.is_deleted = 0 ORDER BY a.create_time DESC")
    IPage<AudioInfo> selectByCreatorId(Long userId, Page<AudioInfo> page);
}
