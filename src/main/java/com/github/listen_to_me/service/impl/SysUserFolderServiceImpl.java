package com.github.listen_to_me.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.listen_to_me.common.exception.BaseException;
import com.github.listen_to_me.common.util.SecurityUtils;
import com.github.listen_to_me.domain.dto.FolderDTO;
import com.github.listen_to_me.domain.entity.Folder;
import com.github.listen_to_me.domain.entity.SysUserFolder;
import com.github.listen_to_me.domain.vo.FolderVO;
import com.github.listen_to_me.mapper.FolderMapper;
import com.github.listen_to_me.mapper.SysUserFolderMapper;
import com.github.listen_to_me.service.SysUserFolderService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
@Service
@AllArgsConstructor
public class SysUserFolderServiceImpl extends ServiceImpl<SysUserFolderMapper, SysUserFolder> implements SysUserFolderService {
    private final FolderMapper folderMapper;
    private final SysUserFolderMapper sysUserFolderMapper;
    @Override
    @Transactional
    public void createFolder(FolderDTO folderDTO) {
        Folder folder = BeanUtil.copyProperties(folderDTO, Folder.class);
        LambdaQueryWrapper<Folder> wrapper = Wrappers.lambdaQuery(Folder.class)
                .eq(Folder::getName, folder.getName());
        if (folderMapper.selectOne(wrapper) != null) {
            throw new BaseException(409, "收藏夹名称已存在");
        }
        folderMapper.insert(folder);
        SysUserFolder sysUserFolder = new SysUserFolder();
        sysUserFolder.setFolderId(folder.getId());
        sysUserFolder.setUserId(SecurityUtils.getCurrentUserId());
        sysUserFolderMapper.insert(sysUserFolder);
    }

    @Override
    public List<FolderVO> getFolderList() {
        Long userId = SecurityUtils.getCurrentUserId();
        LambdaQueryWrapper<SysUserFolder> wrapper = Wrappers.lambdaQuery(SysUserFolder.class)
                .eq(SysUserFolder::getUserId, userId);
        List<SysUserFolder> sysUserFolderList = sysUserFolderMapper.selectList(wrapper);
        if(sysUserFolderList.isEmpty()){
            return List.of();
        }
        List<Folder> folderList = folderMapper.getFolderListOfUsers(sysUserFolderList);
        return folderList.stream()
                .map(folder -> BeanUtil.copyProperties(folder, FolderVO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteFolder(Long folderId) {
        LambdaQueryWrapper<SysUserFolder> wrapper = Wrappers.lambdaQuery(SysUserFolder.class)
                .eq(SysUserFolder::getFolderId, folderId)
                .eq(SysUserFolder::getUserId, SecurityUtils.getCurrentUserId());
        if(!sysUserFolderMapper.exists(wrapper)){
            throw new BaseException(404, "收藏夹不存在");
        }
        sysUserFolderMapper.delete(wrapper);
        folderMapper.deleteById(folderId);
    }
}
