package com.github.listen_to_me.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.listen_to_me.common.Result;
import com.github.listen_to_me.domain.query.AudioQuery;
import com.github.listen_to_me.domain.vo.AudioVO;
import com.github.listen_to_me.service.IAudioInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "音频接口", description = "用户音频相关接口")
@AllArgsConstructor
@RestController
@RequestMapping("/user/audio")
public class UserAudioController {
    private IAudioInfoService iAudioInfoService;

    @GetMapping("/page")
    @Operation(summary = "发现页分页")
    public Result<Page<AudioVO>> getAudioPage(@ParameterObject AudioQuery audioQuery){
        Page<AudioVO> audioVOPage = iAudioInfoService.findAudioPage(audioQuery);
        return Result.success(audioVOPage);
    }
}
