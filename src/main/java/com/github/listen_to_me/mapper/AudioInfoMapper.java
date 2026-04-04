package com.github.listen_to_me.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.listen_to_me.domain.dto.AudioStatsDTO;
import com.github.listen_to_me.domain.entity.AudioInfo;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author kun
 * @since 2026-03-24
 */
public interface AudioInfoMapper extends BaseMapper<AudioInfo> {

    void updateStatusById(Long audioId, String status);

    void updateStatusAndClipPathById(Long audioId, String status, String clipPath);

    @Select("SELECT a.* FROM audio_info a WHERE  a.creator_id = #{userId} AND a.is_deleted = 0 ORDER BY a.create_time DESC")
    IPage<AudioInfo> selectByCreatorId(Long userId, Page<AudioInfo> page);

    @Select("SELECT " +
            "ai.id, " +
            "ai.play_count AS playCount, " +
            "(SELECT COUNT(*) FROM audio_like WHERE audio_id = ai.id) AS likeCount, " +
            "(SELECT COUNT(*) FROM audio_folder_relation WHERE audio_id = ai.id) AS collectCount, " +
            "(SELECT COUNT(*) FROM comment WHERE audio_id = ai.id) AS commentCount " +
            "FROM audio_info ai " +
            "WHERE ai.create_time >= #{oneMonthAgo} " +
            "AND ai.is_deleted = 0 " +
            "AND ai.audit_status = 1")
    List<AudioStatsDTO> selectAudioStatsForHotRank(LocalDateTime oneMonthAgo);
}
