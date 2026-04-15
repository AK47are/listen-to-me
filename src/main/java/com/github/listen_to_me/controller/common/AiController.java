package com.github.listen_to_me.controller.common;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.listen_to_me.common.Result;
import com.github.listen_to_me.domain.vo.AiTaskVO;
import com.github.listen_to_me.service.IAiTaskService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "AI任务")
@RequiredArgsConstructor
@RequestMapping("/common/ai")
@RestController("commonAiController")
public class AiController {

    private final IAiTaskService aiTaskService;

    @GetMapping("/task/{taskId}")
    @Operation(summary = "查询 AI 任务状态")
    public Result<AiTaskVO> getAiTask(
            @AuthenticationPrincipal Long userId,
            @PathVariable String taskId) {
        return Result.success(aiTaskService.getTaskById(userId, taskId));
    }
}
