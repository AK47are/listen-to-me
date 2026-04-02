package com.github.listen_to_me.controller.admin;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.listen_to_me.common.Result;
import com.github.listen_to_me.service.ISysUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "用户管理")
@RequiredArgsConstructor
@RequestMapping("/admin/user")
@RestController("adminUserController")
public class UserController {

    private final ISysUserService sysUserService;

    @PutMapping("/{userId}/ban")
    @Operation(summary = "封禁用户")
    public Result<Void> banUser(@PathVariable Long userId) {
        sysUserService.banUser(userId);
        return Result.success();
    }

    @PutMapping("/{userId}/unban")
    @Operation(summary = "解封用户")
    public Result<Void> unbanUser(@PathVariable Long userId) {
        sysUserService.unbanUser(userId);
        return Result.success();
    }
}
