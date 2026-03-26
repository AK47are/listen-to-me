package com.github.listen_to_me.controller.common;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
