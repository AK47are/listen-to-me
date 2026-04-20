package com.github.listen_to_me.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.houbb.sensitive.word.core.SensitiveWordHelper;
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
import com.github.listen_to_me.domain.entity.SysRole;
import com.github.listen_to_me.domain.entity.SysUser;
import com.github.listen_to_me.domain.entity.SysUserRole;
import com.github.listen_to_me.domain.vo.ImageCaptchaVO;
import com.github.listen_to_me.domain.vo.LoginVO;
import com.github.listen_to_me.domain.vo.UserVO;
import com.github.listen_to_me.mapper.SysRoleMapper;
import com.github.listen_to_me.mapper.SysUserRoleMapper;
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
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

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
        userVO.setAvatar(MinioUtils.getPresignedUrl(user.getAvatar()));

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
        userVO.setAvatar(MinioUtils.getPresignedUrl(user.getAvatar()));

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

    @Transactional
    @Override
    public void addSysUser(UserRegisterDTO userRegisterDTO) {
        if (SensitiveWordHelper.contains(userRegisterDTO.getUsername())) {
            throw new RegisterException(400, "用户名包含敏感词");
        }
        if (StrUtil.isNotBlank(userRegisterDTO.getPhone())
                && StrUtil.isNotBlank(userRegisterDTO.getEmail())) {
            throw new RegisterException(400, "手机号或邮箱不能同时填写");
        }
        if (StrUtil.isBlank(userRegisterDTO.getPhone())
                && StrUtil.isBlank(userRegisterDTO.getEmail())) {
            throw new RegisterException(400, "手机号或邮箱不能同时为空");
        }
        if (StrUtil.isNotBlank(userRegisterDTO.getPhone())) {
            String cachedVerifyCode = RedisUtils.get(RedisKey.VERIFY_CODE, userRegisterDTO.getPhone());
            if (StrUtil.isBlank(cachedVerifyCode)
                    || !cachedVerifyCode.equalsIgnoreCase(userRegisterDTO.getVerifyCode())) {
                throw new RegisterException(400, "校验码错误或已过期");
            }
            RedisUtils.delete(RedisKey.VERIFY_CODE, userRegisterDTO.getPhone());
        }
        if (StrUtil.isNotBlank(userRegisterDTO.getEmail())) {
            String cachedVerifyCode = RedisUtils.get(RedisKey.VERIFY_CODE, userRegisterDTO.getEmail());
            if (StrUtil.isBlank(cachedVerifyCode)
                    || !cachedVerifyCode.equalsIgnoreCase(userRegisterDTO.getVerifyCode())) {
                throw new RegisterException(400, "校验码错误或已过期");
            }
            RedisUtils.delete(RedisKey.VERIFY_CODE, userRegisterDTO.getEmail());
        }
        if (!userRegisterDTO.getPassword().equals(userRegisterDTO.getConfirmPassword())) {
            throw new RegisterException(400, "两次密码不一致");
        }
        userRegisterDTO.setPassword(passwordEncoder.encode(userRegisterDTO.getPassword()));
        SysUser sysUser = BeanUtil.copyProperties(userRegisterDTO, SysUser.class);
        iSysUserService.save(sysUser);
        log.debug("新用户注册 - 用户信息: {}", userRegisterDTO);

        Long roleId = sysRoleMapper.selectOne(Wrappers.<SysRole>lambdaQuery()
                .eq(SysRole::getRoleCode, "ROLE_USER"))
                .getId();
        SysUserRole sysUserRole = new SysUserRole();
        sysUserRole.setUserId(sysUser.getId());
        sysUserRole.setRoleId(roleId);
        sysUserRoleMapper.insert(sysUserRole);
        log.debug("初始化用户角色关联 - 用户ID: {}, 角色ID: {}", sysUser.getId(), roleId);

    }
}
