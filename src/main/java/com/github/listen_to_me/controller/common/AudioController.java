package com.github.listen_to_me.controller.common;

import com.github.listen_to_me.common.Result;
import com.github.listen_to_me.service.IAudioInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/common")
@RequiredArgsConstructor
public class AudioController {
    private final IAudioInfoService audioInfoService;
    @GetMapping("/file/sign")
    public Result<String> getStreamSign(Long audioId){
        return Result.success(audioInfoService.getStreamSign(audioId));
    }
}
