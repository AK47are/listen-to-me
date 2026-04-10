package com.github.listen_to_me.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.listen_to_me.domain.entity.AudioFolderRelation;
import com.github.listen_to_me.domain.vo.FolderVO;

public interface AudioFolderRelationMapper extends BaseMapper<AudioFolderRelation> {

    @Select("SELECT f.* FROM folder f " +
            "INNER JOIN audio_folder_relation afr ON f.id = afr.folder_id " +
            "INNER JOIN sys_user_folder sfu ON sfu.folder_id = f.id " +
            "WHERE afr.audio_id = #{audioId} " +
            "AND sfu.user_id = #{userId}")
    List<FolderVO> selectAudioFolders(Long userId, Long audioId);
}
