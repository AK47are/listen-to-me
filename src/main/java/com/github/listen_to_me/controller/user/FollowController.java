package com.github.listen_to_me.controller.user;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.listen_to_me.common.Result;
import com.github.listen_to_me.domain.query.PageQuery;
import com.github.listen_to_me.domain.vo.CreatorVO;
import com.github.listen_to_me.domain.vo.FansVO;
import com.github.listen_to_me.service.IUserFollowService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/user/creator")
@Tag(name = "创作者关注", description = "包含关注、取消关注等接口")
public class FollowController {

    private final IUserFollowService userFollowService;

    @PostMapping("/{creatorId}/follow")
    @Operation(summary = "关注创作者")
    public Result<Void> follow(@PathVariable Long creatorId,
            @AuthenticationPrincipal Long userId) {
        userFollowService.follow(userId, creatorId);
        return Result.success();
    }

    @DeleteMapping("/{creatorId}/follow")
    @Operation(summary = "取消关注")
    public Result<Void> unfollow(@PathVariable Long creatorId,
            @AuthenticationPrincipal Long userId) {
        userFollowService.unfollow(userId, creatorId);
        return Result.success();
    }

    @GetMapping("/follow/page")
    @Operation(summary = "分页查询关注的创作者列表")
    public Result<IPage<CreatorVO>> getFollowPage(@ParameterObject PageQuery pageQuery,
            @AuthenticationPrincipal Long userId) {
        return Result.success(userFollowService.getFollowPage(userId, pageQuery));
    }

    @GetMapping("/{creatorId}/fans/page")
    @Operation(summary = "分页查询创作者的粉丝列表")
    public Result<IPage<FansVO>> getFansPage(@PathVariable Long creatorId,
            @ParameterObject PageQuery pageQuery) {
        return Result.success(userFollowService.getFansPage(creatorId, pageQuery));
    }
}
