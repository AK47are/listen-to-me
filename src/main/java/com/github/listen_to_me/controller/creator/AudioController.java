package com.github.listen_to_me.controller.creator;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.listen_to_me.common.Result;
import com.github.listen_to_me.domain.dto.AudioDTO;
import com.github.listen_to_me.domain.dto.AudioUpdateDTO;
import com.github.listen_to_me.domain.dto.CreatorAudioDetailVO;
import com.github.listen_to_me.domain.query.PageQuery;
import com.github.listen_to_me.domain.vo.AudioPublishVO;
import com.github.listen_to_me.domain.vo.AudioStatusVO;
import com.github.listen_to_me.domain.vo.CreatorAudioVO;
import com.github.listen_to_me.service.IAudioInfoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RequestMapping("/creator/audio")
@RestController("creatorAudioController")
@Tag(name = "音频稿件管理", description = "包含上传音频、上传封面等接口")
public class AudioController {
    private final IAudioInfoService audioInfoService;

    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('audio:upload')")
    @Operation(summary = "上传音频", description = "上传 MP3 格式的音频文件")
    public Result<String> uploadAudio(
            @Parameter(description = "音频文件", required = true) MultipartFile audioFile) throws Exception {
        return Result.success(audioInfoService.uploadAudio(audioFile));
    }

    @PostMapping("/cover/upload")
    @PreAuthorize("hasAuthority('cover:upload')")
    @Operation(summary = "上传封面", description = "上传 JPG、JPEG、PNG 格式的封面图片")
    public Result<String> uploadCover(
            @Parameter(description = "封面文件", required = true) MultipartFile coverFile) throws Exception {
        return Result.success(audioInfoService.uploadCover(coverFile));
    }

    @PostMapping
    @Operation(summary = "发布音频稿件", description = "创建音频稿件，提交后进入转码流程")
    public Result<AudioPublishVO> saveAudio(
            @Parameter(description = "音频稿件信息", required = true) @RequestBody AudioDTO audioDTO) throws Exception {
        return Result.success(audioInfoService.saveAudio(audioDTO));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询我的稿件")
    public Result<IPage<CreatorAudioVO>> getAudioPage(
            @AuthenticationPrincipal Long userId,
            @ParameterObject PageQuery pageQuery) {
        return Result.success(audioInfoService.getAudioPage(userId, pageQuery));
    }

    @GetMapping("/{audioId}")
    @Operation(summary = "获取稿件详情（用于修改）")
    public Result<CreatorAudioDetailVO> getAudio(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long audioId) {
        return Result.success(audioInfoService.getCreatorAudioDetail(userId, audioId));
    }

    @GetMapping("/{id}/status")
    public Result<AudioStatusVO> getAudioStatus(@PathVariable Long id) {
        return Result.success(audioInfoService.getAudioStatus(id));
    }

    @PutMapping
    public Result<Void> updateAudio(@RequestBody AudioUpdateDTO audioUpdateDTO) {
        audioInfoService.updateAudio(audioUpdateDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> removeAudio(@PathVariable Long id) {
        audioInfoService.removeAudioInfo(id);
        return Result.success();
    }

}
