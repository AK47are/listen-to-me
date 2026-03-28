package com.github.listen_to_me.controller.user;

import com.github.listen_to_me.common.Result;
import com.github.listen_to_me.domain.dto.UserProfileUpdateDTO;
import com.github.listen_to_me.domain.vo.UserVO;
import com.github.listen_to_me.service.ISysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;


@Tag(name = "用户接口", description = "用户相关接口包含用户信息查询、用户信息更新等操作")
@RestController
@RequestMapping("/user")
@AllArgsConstructor
public class UserController {

    private ISysUserService sysUserService;

    @Operation(summary = "获取当前登录用户资料")
    @GetMapping("/profile")
    public Result<UserVO> getProfile(){
        return Result.success(sysUserService.findProfile());
    }

    @Operation(summary = "修改个人资料")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody UserProfileUpdateDTO updateDTO){
        sysUserService.modifyProfile(updateDTO);
        return Result.success();
    }
}
