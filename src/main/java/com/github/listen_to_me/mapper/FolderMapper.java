package com.github.listen_to_me.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.listen_to_me.domain.entity.Folder;
import com.github.listen_to_me.domain.entity.SysUserFolder;

import java.util.List;

public interface FolderMapper extends BaseMapper<Folder> {
    List<Folder> getFolderListOfUsers(List<SysUserFolder> sysUserFolders);
}
