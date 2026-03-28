package com.github.listen_to_me.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.listen_to_me.domain.dto.FolderDTO;
import com.github.listen_to_me.domain.entity.SysUserFolder;
import com.github.listen_to_me.domain.vo.FolderVO;

import java.util.List;

public interface SysUserFolderService extends IService<SysUserFolder> {
    void createFolder(FolderDTO folderDTO);

    List<FolderVO> getFolderList();

    void deleteFolder(Long folderId);
}
