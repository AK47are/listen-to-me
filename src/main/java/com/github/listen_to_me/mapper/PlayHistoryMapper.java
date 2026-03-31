package com.github.listen_to_me.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.listen_to_me.domain.entity.PlayHistory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.listen_to_me.domain.vo.AudioVO;
import org.apache.ibatis.annotations.Insert;
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
public interface PlayHistoryMapper extends BaseMapper<PlayHistory> {

    @Select({
            "SELECT * FROM audio_info ai ",
            "INNER JOIN play_history ph ON ai.id = ph.audio_id ",
            "WHERE ph.user_id = #{userId} ORDER BY ph.update_time DESC"
    })
    IPage<AudioVO> selectHistoryPage(Page<AudioVO> page, @Param("userId") Long userId);

    @Insert("INSERT INTO play_history (user_id, audio_id, last_position, update_time) " +
            "VALUES (#{history.userId}, #{history.audioId}, #{history.lastPosition}, NOW()) " +
            "ON DUPLICATE KEY UPDATE " +
            "last_position = VALUES(last_position), " +
            "update_time = NOW()")
    boolean insertOrUpdate(@Param("history") PlayHistory history);
}

