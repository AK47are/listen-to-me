package com.github.listen_to_me.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.listen_to_me.domain.entity.PlayHistory;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author kun
 * @since 2026-03-24
 */
public interface PlayHistoryMapper extends BaseMapper<PlayHistory> {

    @Insert("INSERT INTO play_history (user_id, audio_id, last_position, update_time) " +
            "VALUES (#{history.userId}, #{history.audioId}, #{history.lastPosition}, NOW()) " +
            "ON DUPLICATE KEY UPDATE " +
            "last_position = VALUES(last_position), " +
            "update_time = NOW()")
    boolean insertOrUpdate(@Param("history") PlayHistory history);
}
