package com.github.listen_to_me.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.listen_to_me.domain.dto.AudioStatsDTO;
import com.github.listen_to_me.domain.entity.AudioInfo;
import com.github.listen_to_me.domain.vo.AuditAudioVO;
import com.github.listen_to_me.domain.vo.CreatorAudioVO;

public interface AudioInfoMapper extends BaseMapper<AudioInfo> {

    void updateStatusById(Long audioId, String status);

    void completeTranscode(Long audioId, String clipPath, Integer duration);

    @Select("SELECT a.* FROM audio_info a WHERE  a.creator_id = #{userId} AND a.is_deleted = 0 ORDER BY a.create_time DESC")
    IPage<AudioInfo> selectByCreatorId(Long userId, Page<AudioInfo> page);

    @Select("SELECT " +
            "ai.id, " +
            "ai.play_count AS playCount, " +
            "ai.like_count AS likeCount, " +
            "ai.collect_count AS collectCount, " +
            "ai.comment_count AS commentCount " +
            "FROM audio_info ai " +
            "WHERE ai.create_time >= #{oneMonthAgo} " +
            "AND ai.is_deleted = 0 " +
            "AND ai.audit_status = 'APPROVED'")
    List<AudioStatsDTO> selectAudioStatsForHotRank(LocalDateTime oneMonthAgo);

    IPage<AuditAudioVO> selectAuditAudioPage(Page<AuditAudioVO> page, String status);

    IPage<CreatorAudioVO> selectCreatorAudioPage(Page<CreatorAudioVO> page, Long userId);

    @Update("UPDATE audio_info SET play_count = play_count + #{delta} WHERE id = #{audioId}")
    void incrementPlayCount(Long audioId, int delta);

    @Update("UPDATE audio_info SET like_count = like_count + #{delta} WHERE id = #{audioId}")
    void incrementLikeCount(Long audioId, int delta);

    @Update("UPDATE audio_info SET collect_count = collect_count + #{delta} WHERE id = #{audioId}")
    void incrementCollectCount(Long audioId, int delta);

    @Update("UPDATE audio_info SET comment_count = comment_count + #{delta} WHERE id = #{audioId}")
    void incrementCommentCount(Long audioId, int delta);
}
