package com.github.listen_to_me.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.github.listen_to_me.common.enumeration.RedisKey;
import com.github.listen_to_me.common.util.JwtUtils;
import com.github.listen_to_me.common.util.MinioUtils;
import com.github.listen_to_me.common.util.RedisUtils;
import com.github.listen_to_me.domain.SysUserAdapter;
import com.github.listen_to_me.domain.dto.LoginDTO;
import com.github.listen_to_me.domain.entity.SysUser;
import com.github.listen_to_me.domain.vo.ImageCaptchaVO;
import com.github.listen_to_me.domain.vo.LoginVO;
import com.github.listen_to_me.domain.vo.UserVO;
import com.github.listen_to_me.service.AuthService;
import com.github.listen_to_me.service.ISysUserService;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final ISysUserService iSysUserService;

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

    @Override
    public LoginVO refreshToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        // 更新用户 Token 的同时更新一遍用户数据
        SysUser user = iSysUserService.getById(userId);

        String newToken = JwtUtils.generateToken(
                user.getId().toString(),
                user.getUsername());

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        if (StrUtil.isNotBlank(user.getAvatar())) {
            try {
                userVO.setAvatar(MinioUtils.getPresignedUrl(user.getAvatar()));
            } catch (Exception e) {
                log.error("生成头像失败 - 错误信息: {}", e.getMessage());
            }
        }

        LoginVO vo = new LoginVO();
        vo.setToken(newToken);
        vo.setUserInfo(userVO);

        log.info("刷新令牌 - 用户: {}", user.getUsername());
        return vo;
    }

    @Override
    public ImageCaptchaVO createImageCaptcha() {
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(200, 100, 4, 20);
        String uuid = IdUtil.simpleUUID();

        RedisUtils.set(RedisKey.CAPTCHA, uuid, lineCaptcha.getCode());

        return ImageCaptchaVO.builder()
                .uuid(uuid)
                .img(lineCaptcha.getImageBase64Data())
                .build();
    }

}
