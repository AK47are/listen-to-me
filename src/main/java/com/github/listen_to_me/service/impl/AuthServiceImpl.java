package com.github.listen_to_me.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.github.listen_to_me.common.enumeration.RedisKey;
import com.github.listen_to_me.common.exception.AuthException;
import com.github.listen_to_me.common.exception.BaseException;
import com.github.listen_to_me.common.exception.RegisterException;
import com.github.listen_to_me.common.util.JwtUtils;
import com.github.listen_to_me.common.util.MinioUtils;
import com.github.listen_to_me.common.util.RedisUtils;
import com.github.listen_to_me.domain.SysUserAdapter;
import com.github.listen_to_me.domain.dto.LoginDTO;
import com.github.listen_to_me.domain.dto.UserRegisterDTO;
import com.github.listen_to_me.domain.dto.VerifyCodeDTO;
import com.github.listen_to_me.domain.entity.SysUser;
import com.github.listen_to_me.domain.vo.ImageCaptchaVO;
import com.github.listen_to_me.domain.vo.LoginVO;
import com.github.listen_to_me.domain.vo.UserVO;
import com.github.listen_to_me.service.AuthService;
import com.github.listen_to_me.service.ISysUserService;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.bean.BeanUtil;
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
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginVO loginUser(LoginDTO loginDTO) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword()));

        SysUserAdapter userAdapter = (SysUserAdapter) auth.getPrincipal();
        SysUser user = userAdapter.getSysUser();

        if ("BANNED".equals(user.getStatus())) {
            log.debug("用户已被封禁 - ID: {}", user.getId());
            throw new BaseException(403, "账号已被封禁");
        }

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

        RedisUtils.set(RedisKey.IMAGE_CAPTCHA, uuid, lineCaptcha.getCode());

        return ImageCaptchaVO.builder()
                .uuid(uuid)
                .img(lineCaptcha.getImageBase64Data())
                .build();
    }

    @Override
    public void sendVerifyCode(VerifyCodeDTO verifyCodeDTO) {
        String cachedCode = RedisUtils.get(RedisKey.IMAGE_CAPTCHA, verifyCodeDTO.getUuid());
        if (cachedCode == null || !cachedCode.equalsIgnoreCase(verifyCodeDTO.getImageCode())) {
            throw new AuthException("图形验证码错误或已过期");
        }
        RedisUtils.delete(RedisKey.IMAGE_CAPTCHA, verifyCodeDTO.getUuid());

        String verifyCode = cn.hutool.core.util.RandomUtil.randomNumbers(6);

        RedisUtils.set(RedisKey.VERIFY_CODE, verifyCodeDTO.getTarget(), verifyCode);
        // TODO: 接入真实的手机号/邮箱发送平台
        log.debug("校验码发送 - 目标: {}, 验证码: {}", verifyCodeDTO.getTarget(), verifyCode);
    }

    @Override
    public void addSysUser(UserRegisterDTO userRegisterDTO) {
        if (StrUtil.isBlank(userRegisterDTO.getUsername())) {
            throw new RegisterException(400, "用户名不能为空");
        }
        String target = StrUtil.isBlank(userRegisterDTO.getPhone())
                ? userRegisterDTO.getEmail()
                : userRegisterDTO.getPhone();
        if (StrUtil.isBlank(target)) {
            throw new RegisterException(400, "手机号或邮箱不能为空");
        }
        if (StrUtil.isBlank(userRegisterDTO.getPassword())) {
            throw new RegisterException(400, "密码不能为空");
        }
        if (!userRegisterDTO.getPassword().equals(userRegisterDTO.getConfirmPassword())) {
            throw new RegisterException(400, "两次密码不一致");
        }

        String cachedVerifyCode = RedisUtils.get(RedisKey.VERIFY_CODE, target);
        if (StrUtil.isBlank(cachedVerifyCode) || !cachedVerifyCode.equalsIgnoreCase(userRegisterDTO.getVerifyCode())) {
            throw new RegisterException(400, "校验码错误或已过期");
        }
        RedisUtils.delete(RedisKey.VERIFY_CODE, target);
        userRegisterDTO.setPassword(passwordEncoder.encode(userRegisterDTO.getPassword()));
        iSysUserService.save(BeanUtil.copyProperties(userRegisterDTO, SysUser.class));
        log.debug("新用户注册 - 用户信息: {}", userRegisterDTO);

        // FIX: 需要初始化其它关联表
    }
}
