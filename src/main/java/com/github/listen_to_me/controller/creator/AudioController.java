package com.github.listen_to_me.controller.creator;

import com.github.listen_to_me.common.Result;
import com.github.listen_to_me.domain.dto.AudioDTO;
import com.github.listen_to_me.domain.vo.AudioPublishVO;
import com.github.listen_to_me.service.IAudioInfoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@RequestMapping("/creator/audio")
@RestController("creatorAudioController")
@Tag(name = "音频稿件管理", description = "包含上传音频、上传封面等接口")
public class AudioController {
    private final IAudioInfoService audioInfoService;
    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('audio:upload')")
    public Result<String> uploadAudio(MultipartFile audioFile) throws Exception {

        return Result.success(audioInfoService.uploadAudio(audioFile));
    }

    @PostMapping("/cover/upload")
    @PreAuthorize("hasAuthority('cover:upload')")
    public Result<String> uploadCover(MultipartFile coverFile) throws Exception {
        return Result.success(audioInfoService.uploadCover(coverFile));
    }

    @PostMapping
    public Result<AudioPublishVO> saveAudio(@RequestBody AudioDTO audioDTO) throws Exception {
        return Result.success(audioInfoService.saveAudio(audioDTO));
    }
}
