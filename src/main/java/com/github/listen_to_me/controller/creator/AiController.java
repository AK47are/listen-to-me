package com.github.listen_to_me.controller.creator;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
@Tag(name = "AI增强")
@RequiredArgsConstructor
@RequestMapping("/creator/ai")
@RestController("creatorAiController")
public class AiController {

    private final IAiTaskService aiTaskService;

    @PostMapping("/transcript/{audioId}")
    @Operation(summary = "申请 AI 转写")
    public Result<AiTaskVO> saveAiTranscript(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long audioId) {
        AiTaskVO vo = aiTaskService.createTranscriptionTask(userId, audioId);
        return Result.success(vo);
    }

    @PutMapping("/transcript/{taskId}/confirm")
    @Operation(summary = "确认转写结果")
    public Result<Void> confirmTranscript(@AuthenticationPrincipal Long userId,
            @PathVariable String taskId) {
        aiTaskService.confirmTranscript(userId, taskId);
        return Result.success();
    }
}
