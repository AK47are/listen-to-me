package com.github.listen_to_me.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.listen_to_me.domain.entity.SysUserFolder;
import com.github.listen_to_me.mapper.SysUserFolderMapper;
import com.github.listen_to_me.service.SysUserFolderService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
@Service
@AllArgsConstructor
public class SysUserFolderServiceImpl extends ServiceImpl<SysUserFolderMapper, SysUserFolder> implements SysUserFolderService {

}
