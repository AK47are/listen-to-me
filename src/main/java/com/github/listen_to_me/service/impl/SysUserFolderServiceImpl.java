package com.github.listen_to_me.service.impl;

import java.util.List;
import java.util.stream.Collectors;

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
import com.github.listen_to_me.domain.entity.SysUserFolder;
import com.github.listen_to_me.domain.vo.FolderVO;
import com.github.listen_to_me.mapper.AudioFolderRelationMapper;
import com.github.listen_to_me.mapper.FolderMapper;
import com.github.listen_to_me.mapper.SysUserFolderMapper;
import com.github.listen_to_me.service.ISysUserFolderService;

import cn.hutool.core.bean.BeanUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class SysUserFolderServiceImpl extends ServiceImpl<SysUserFolderMapper, SysUserFolder>
        implements ISysUserFolderService {
    private final FolderMapper folderMapper;
    private final SysUserFolderMapper sysUserFolderMapper;
    private final AudioFolderRelationMapper audioFolderRelationMapper;

    @Override
    @Transactional
    public void createFolder(Long userId, FolderDTO folderDTO) {
        Folder folder = BeanUtil.copyProperties(folderDTO, Folder.class);
        folderMapper.insert(folder);
        SysUserFolder sysUserFolder = new SysUserFolder();
        sysUserFolder.setFolderId(folder.getId());
        sysUserFolder.setUserId(userId);
        sysUserFolderMapper.insert(sysUserFolder);
    }

    @Override
    public List<FolderVO> getFolderList() {
        Long userId = SecurityUtils.getCurrentUserId();
        LambdaQueryWrapper<SysUserFolder> wrapper = Wrappers.lambdaQuery(SysUserFolder.class)
                .eq(SysUserFolder::getUserId, userId);
        List<SysUserFolder> sysUserFolderList = sysUserFolderMapper.selectList(wrapper);
        if (sysUserFolderList.isEmpty()) {
            return List.of();
        }
        List<Folder> folderList = folderMapper.getFolderListOfUsers(sysUserFolderList);
        return folderList.stream()
                .map(folder -> BeanUtil.copyProperties(folder, FolderVO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteFolder(Long userId, Long folderId) {
        LambdaQueryWrapper<SysUserFolder> wrapper = Wrappers.lambdaQuery(SysUserFolder.class)
                .eq(SysUserFolder::getFolderId, folderId)
                .eq(SysUserFolder::getUserId, userId);
        if (!sysUserFolderMapper.exists(wrapper)) {
            throw new BaseException(404, "收藏夹不存在");
        }

        sysUserFolderMapper.delete(wrapper);
        audioFolderRelationMapper.delete(Wrappers.lambdaQuery(AudioFolderRelation.class)
                .eq(AudioFolderRelation::getFolderId, folderId));
        folderMapper.deleteById(folderId);
        log.debug("删除收藏夹成功 - 收藏夹ID: {}, 用户ID: {}", folderId, userId);
    }
}
