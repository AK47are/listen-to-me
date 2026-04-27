package com.github.listen_to_me.controller.user;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.listen_to_me.common.Result;
import com.github.listen_to_me.domain.dto.HistoryProgressDTO;
import com.github.listen_to_me.domain.query.PageQuery;
import com.github.listen_to_me.domain.vo.AudioVO;
import com.github.listen_to_me.service.IPlayHistoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Tag(name = "用户播放历史")
@Validated
@RestController
@RequestMapping("/user/history")
@RequiredArgsConstructor
public class HistoryController {
    private final IPlayHistoryService playHistoryService;

    @PostMapping
    @Operation(summary = "记录播放历史")
    public Result<Void> saveHistory(@AuthenticationPrincipal Long userId,
            @Valid @RequestBody HistoryProgressDTO historyProgressDTO) {
        playHistoryService.addPlayHistory(userId, historyProgressDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询历史记录")
    public Result<IPage<AudioVO>> getHistoryPage(@AuthenticationPrincipal Long userId,
            @Valid @ParameterObject PageQuery pageQuery) {
        return Result.success(playHistoryService.getHistoryPage(userId, pageQuery));
    }

    @GetMapping("/{audioId}")
    @Operation(summary = "获取播放历史")
    public Result<Integer> getHistory(@AuthenticationPrincipal Long userId, @PathVariable @Positive Long audioId) {
        return Result.success(playHistoryService.findPlayHistory(userId, audioId));
    }
}
