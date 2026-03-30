package com.github.listen_to_me.controller.creator;

import com.github.listen_to_me.common.Result;
import com.github.listen_to_me.service.IAudioInfoService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
@RequestMapping("/creator")
public class AudioController {
    private final IAudioInfoService audioInfoService;
    @PostMapping("/audio/upload")
    @PreAuthorize("hasAuthority('audio:upload')")
    public Result<String> uploadAudio(MultipartFile audioFile) throws Exception {

        return Result.success(audioInfoService.uploadAudio(audioFile));
    }
}
