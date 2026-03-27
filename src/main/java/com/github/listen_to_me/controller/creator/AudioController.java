package com.github.listen_to_me.controller.creator;

import com.github.listen_to_me.common.Result;
import com.github.listen_to_me.domain.dto.AudioDTO;
import com.github.listen_to_me.domain.entity.AudioInfo;
import com.github.listen_to_me.service.IAudioInfoService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/creator/audio")
@Slf4j
@AllArgsConstructor
public class AudioController {
    private final IAudioInfoService audioInfoService;

    @PostMapping("/save")
    public Result saveAudio(@RequestBody AudioDTO audioDTO) {
        audioInfoService.addAudio(audioDTO);

        return Result.success("上传成功");
    }

    @PostMapping("/upload")
    public Result<String> uploadAudio(@RequestParam("file") MultipartFile file) throws Exception {
        if (!file.getOriginalFilename().endsWith(".mp3")) {
            return Result.fail("文件类型错误，仅支持 mp3 格式");
        }
        return Result.success(audioInfoService.uploadAudio(file));
    }
}
