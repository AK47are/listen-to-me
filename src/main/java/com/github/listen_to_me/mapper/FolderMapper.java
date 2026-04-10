package com.github.listen_to_me.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.listen_to_me.domain.entity.Folder;
import com.github.listen_to_me.domain.entity.SysUserFolder;

public interface FolderMapper extends BaseMapper<Folder> {
    List<Folder> getFolderListOfUsers(List<SysUserFolder> sysUserFolders);

    @Update("UPDATE folder SET audio_count = audio_count + #{delta} WHERE id = #{folderId}")
    void incrementAudioCount(Long folderId, int delta);
}
