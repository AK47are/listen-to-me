package com.github.listen_to_me.controller.user;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.listen_to_me.common.Result;
import com.github.listen_to_me.domain.query.PageQuery;
import com.github.listen_to_me.domain.vo.AudioVO;
import com.github.listen_to_me.service.IAudioLikeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/user/like")
@Tag(name = "喜欢管理", description = "包含查询喜欢等接口")
public class LikeController {

    private final IAudioLikeService audioLikeService;

    @GetMapping("/page")
    @Operation(summary = "获取喜欢音频列表")
    public Result<IPage<AudioVO>> getLikePage(@ParameterObject PageQuery pageQuery) {
        return Result.success(audioLikeService.getLikePage(pageQuery));
    }
}
