package com.github.listen_to_me.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.listen_to_me.common.util.JwtUtils;
import com.github.listen_to_me.common.util.MinioUtils;
import com.github.listen_to_me.domain.SysUserAdapter;
import com.github.listen_to_me.domain.dto.LoginDTO;
import com.github.listen_to_me.domain.entity.SysUser;
import com.github.listen_to_me.domain.vo.LoginVO;
import com.github.listen_to_me.domain.vo.UserVO;
import com.github.listen_to_me.mapper.SysUserMapper;
import com.github.listen_to_me.service.ISysUserService;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private final AuthenticationManager authenticationManager;

    @Override
    public LoginVO loginUser(LoginDTO loginDTO) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword()));

        SysUserAdapter userAdapter = (SysUserAdapter) auth.getPrincipal();
        SysUser user = userAdapter.getSysUser();

        String token = JwtUtils.generateToken(
                user.getId().toString(),
                user.getUsername());

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);

        if (StrUtil.isNotBlank(user.getAvatar())) {
            try {
                String tempUrl = MinioUtils.getPresignedUrl(user.getAvatar());
                userVO.setAvatar(tempUrl);
            } catch (Exception e) {
                log.error("生成头像失败 - 错误信息: {}", e.getMessage());
                userVO.setAvatar(null);
            }
        }

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserInfo(userVO);

        return loginVO;
    }
}
