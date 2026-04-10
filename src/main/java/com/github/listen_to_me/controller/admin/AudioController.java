package com.github.listen_to_me.controller.admin;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.listen_to_me.common.Result;
import com.github.listen_to_me.domain.dto.AudioAuditDTO;
import com.github.listen_to_me.domain.query.AuditQuery;
import com.github.listen_to_me.domain.vo.AuditAudioVO;
import com.github.listen_to_me.service.IAudioInfoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@Tag(name = "音频审核")
@RequestMapping("/admin/audio/audit")
@RestController("adminAudioController")
@AllArgsConstructor
public class AudioController {

    private final IAudioInfoService audioInfoService;

    @GetMapping("/page")
    @Operation(summary = "获取音频审核分页列表")
    public Result<IPage<AuditAudioVO>> getAuditAudioPage(@ParameterObject AuditQuery auditQuery) {
        return Result.success(audioInfoService.getAuditAudioPage(auditQuery));
    }

    @PostMapping
    @Operation(summary = "审核音频")
    public Result<Void> auditAudio(@Valid @RequestBody AudioAuditDTO audioAuditDTO) {
        audioInfoService.auditAudio(audioAuditDTO);
        return Result.success();
    }
}
