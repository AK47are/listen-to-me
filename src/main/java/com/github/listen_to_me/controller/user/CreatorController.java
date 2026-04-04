package com.github.listen_to_me.controller.user;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.listen_to_me.common.Result;
import com.github.listen_to_me.domain.query.CreatorPageQuery;
import com.github.listen_to_me.domain.vo.CreatorVO;
import com.github.listen_to_me.service.ICreatorProfileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/user/creator")
@Tag(name = "创作者", description = "包含创作者列表查询等接口")
public class CreatorController {

    private final ICreatorProfileService creatorProfileService;

    @GetMapping("/page")
    @Operation(summary = "分页查询创作者列表")
    public Result<IPage<CreatorVO>> getCreatorPage(@ParameterObject CreatorPageQuery query,
            @AuthenticationPrincipal Long userId) {
        return Result.success(creatorProfileService.getCreatorPage(userId, query));
    }
}
