package com.github.listen_to_me.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.listen_to_me.domain.entity.Folder;
import com.github.listen_to_me.mapper.FolderMapper;
import com.github.listen_to_me.service.IFolderService;
import org.springframework.stereotype.Service;

@Service
public class FolderServiceImpl extends ServiceImpl<FolderMapper, Folder> implements IFolderService {
}
