package com.github.listen_to_me.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.listen_to_me.domain.dto.FolderDTO;
import com.github.listen_to_me.domain.entity.Folder;
import com.github.listen_to_me.domain.vo.FolderVO;

public interface IFolderService extends IService<Folder> {
    List<FolderVO> getUserFolders();

    FolderVO createFolder(String name);

    void createFolder(Long userId, FolderDTO folderDTO);

    List<FolderVO> getFolderList(Long userId);

    void deleteFolder(Long userId, Long folderId);
}
