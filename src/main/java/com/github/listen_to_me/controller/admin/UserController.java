package com.github.listen_to_me.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.listen_to_me.domain.query.UserPageQuery;
import com.github.listen_to_me.domain.vo.UserVO;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/page")
    @Operation(summary = "分页查询用户")
    public Result<IPage<UserVO>> getUserPage(@ParameterObject UserPageQuery query) {
        return Result.success(sysUserService.getUserPage(query));
    }

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
