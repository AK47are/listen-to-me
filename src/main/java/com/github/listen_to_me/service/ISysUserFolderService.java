package com.github.listen_to_me.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.listen_to_me.domain.dto.FolderDTO;
import com.github.listen_to_me.domain.entity.SysUserFolder;
import com.github.listen_to_me.domain.vo.FolderVO;

public interface ISysUserFolderService extends IService<SysUserFolder> {

    void createFolder(Long userId, FolderDTO folderDTO);

    List<FolderVO> getFolderList();

    void deleteFolder(Long userId, Long folderId);
}
