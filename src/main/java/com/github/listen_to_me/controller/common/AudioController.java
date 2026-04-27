package com.github.listen_to_me.controller.common;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.listen_to_me.common.Result;
import com.github.listen_to_me.service.IAudioInfoService;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/common")
@RequiredArgsConstructor
public class AudioController {
    private final IAudioInfoService audioInfoService;

    @GetMapping("/file/sign")
    public Result<String> getStreamSign(@AuthenticationPrincipal Long userId, @RequestParam @NotNull @Positive Long audioId) {
        return Result.success(audioInfoService.getStreamSign(userId, audioId));
    }
}
