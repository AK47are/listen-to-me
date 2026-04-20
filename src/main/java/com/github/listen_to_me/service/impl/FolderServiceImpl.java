package com.github.listen_to_me.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.listen_to_me.common.exception.BaseException;
import com.github.listen_to_me.common.util.SecurityUtils;
import com.github.listen_to_me.domain.dto.FolderDTO;
import com.github.listen_to_me.domain.entity.AudioFolderRelation;
import com.github.listen_to_me.domain.entity.Folder;
import com.github.listen_to_me.domain.vo.FolderVO;
import com.github.listen_to_me.mapper.AudioFolderRelationMapper;
import com.github.listen_to_me.mapper.FolderMapper;
import com.github.listen_to_me.service.IFolderService;

import cn.hutool.core.bean.BeanUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class FolderServiceImpl extends ServiceImpl<FolderMapper, Folder> implements IFolderService {
    private final FolderMapper folderMapper;
    private final AudioFolderRelationMapper audioFolderRelationMapper;

    @Override
    public List<FolderVO> getUserFolders() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Folder> folders = folderMapper.selectList(Wrappers.lambdaQuery(Folder.class)
                .eq(Folder::getUserId, userId));
        return folders.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public FolderVO createFolder(String name) {
        Long userId = SecurityUtils.getCurrentUserId();
        Folder folder = new Folder();
        folder.setUserId(userId);
        folder.setName(name);
        folder.setAudioCount(0L);
        save(folder);
        return convertToVO(folder);
    }

    private FolderVO convertToVO(Folder folder) {
        if (folder == null) {
            return null;
        }
        FolderVO vo = new FolderVO();
        BeanUtils.copyProperties(folder, vo);
        return vo;
    }

    @Override
    @Transactional
    public void createFolder(Long userId, FolderDTO folderDTO) {
        Folder folder = BeanUtil.copyProperties(folderDTO, Folder.class);
        folder.setUserId(userId);
        folderMapper.insert(folder);
        log.debug("创建收藏夹成功 - 用户ID: {}, 收藏夹ID: {}", userId, folder.getId());
    }

    @Override
    public List<FolderVO> getFolderList(Long userId) {
        LambdaQueryWrapper<Folder> wrapper = Wrappers.lambdaQuery(Folder.class)
                .eq(Folder::getUserId, userId);
        List<Folder> folderList = folderMapper.selectList(wrapper);
        return folderList.stream()
                .map(folder -> BeanUtil.copyProperties(folder, FolderVO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteFolder(Long userId, Long folderId) {
        LambdaQueryWrapper<Folder> wrapper = Wrappers.lambdaQuery(Folder.class)
                .eq(Folder::getId, folderId)
                .eq(Folder::getUserId, userId);
        if (folderMapper.selectCount(wrapper) == 0) {
            throw new BaseException(404, "收藏夹不存在");
        }

        audioFolderRelationMapper.delete(Wrappers.lambdaQuery(AudioFolderRelation.class)
                .eq(AudioFolderRelation::getFolderId, folderId));
        folderMapper.deleteById(folderId);
        log.debug("删除收藏夹成功 - 收藏夹ID: {}, 用户ID: {}", folderId, userId);
    }
}
