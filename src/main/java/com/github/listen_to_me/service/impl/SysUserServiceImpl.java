package com.github.listen_to_me.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.github.listen_to_me.common.exception.BaseException;
import com.github.listen_to_me.common.util.MinioUtils;
import com.github.listen_to_me.common.util.SecurityUtils;
import com.github.listen_to_me.domain.vo.UserVO;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.listen_to_me.domain.entity.SysUser;
import com.github.listen_to_me.mapper.SysUserMapper;
import com.github.listen_to_me.service.ISysUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    @Override
    public UserVO findProfile() {
        Long currId = SecurityUtils.getCurrentUserId();
        log.debug("查询详情 - ID: {}", currId);
        SysUser sysUser = this.getById(currId);
        try {
            sysUser.setAvatar(MinioUtils.getPresignedUrl(sysUser.getAvatar()));
        } catch (Exception e) {
            throw new BaseException("头像 url 获取异常！");
        }
        UserVO userVO = BeanUtil.copyProperties(sysUser, UserVO.class);
        log.debug("查询详情 - ID: {}, 用户信息: {}", currId, userVO);
        return userVO;
    }
}
