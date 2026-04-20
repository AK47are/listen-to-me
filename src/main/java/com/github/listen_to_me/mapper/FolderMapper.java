package com.github.listen_to_me.mapper;

import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.listen_to_me.domain.entity.Folder;

public interface FolderMapper extends BaseMapper<Folder> {
    @Update("UPDATE folder SET audio_count = audio_count + #{delta} WHERE id = #{folderId}")
    void incrementAudioCount(Long folderId, int delta);

}
