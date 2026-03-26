package com.github.listen_to_me.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.listen_to_me.common.enumeration.RedisKey;
import com.github.listen_to_me.common.util.RedisUtils;
import com.github.listen_to_me.domain.SysUserAdapter;
import com.github.listen_to_me.domain.entity.SysUser;
import com.github.listen_to_me.mapper.SysUserMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 安全认证服务实现类，用来为 Spring Security 框架提供 UserDetails 对象
 */
@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private SysUserMapper sysUserMapper;

    // 根据用户名定位用户并加载其完整权限列表
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("定位用户 - 账号: {}", username);
        SysUser user = sysUserMapper.selectOne(Wrappers.lambdaQuery(SysUser.class)
                .eq(SysUser::getUsername, username));
        if (user == null) {
            log.debug("用户不存在 - 账号: {}", username);
            // TODO: 待 SecurityConfig 完备后，通过 AuthenticationEntryPoint 统一将此异常映射为
            // BaseException 格式。目前保持原生异常以确保 Spring Security 认证过滤器链能正常识别身份缺失信号
            throw new UsernameNotFoundException("用户账号不存在: " + username);
        }

        List<String> permCodeList = sysUserMapper.selectPermCodeListById(user.getId());
        RedisUtils.set(RedisKey.USER_PERMS, user.getId().toString(), permCodeList);

        log.debug("认证信息就绪 - 账号: {}, 权限: {}", username, permCodeList);
        return new SysUserAdapter(user, permCodeList);
    }

}
