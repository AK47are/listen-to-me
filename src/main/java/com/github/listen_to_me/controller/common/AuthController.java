package com.github.listen_to_me.controller.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.listen_to_me.common.Result;
import com.github.listen_to_me.domain.dto.LoginDTO;
import com.github.listen_to_me.domain.vo.ImageCaptchaVO;
import com.github.listen_to_me.domain.vo.LoginVO;
import com.github.listen_to_me.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 公共认证控制器
 */
@RestController
@RequestMapping("/common/auth")
@RequiredArgsConstructor
@Tag(name = "身份认证", description = "包含登录、注册、验证码等接口")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "密码登录", description = "提交用户名密码，返回 JWT 令牌及用户信息")
    public Result<LoginVO> login(@RequestBody LoginDTO loginDTO) {
        return Result.success(authService.loginUser(loginDTO));
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新 Token", description = "返回新 Token 和用户信息")
    public Result<LoginVO> refreshToken() {
        return Result.success(authService.refreshToken());
    }

    @GetMapping("/image-captcha")
    @Operation(summary = "获取图形验证码")
    public Result<ImageCaptchaVO> getImageCaptcha() {
        return Result.success(authService.createImageCaptcha());
    }
}
