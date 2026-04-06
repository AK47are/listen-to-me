package com.github.listen_to_me.controller.user;

import com.github.listen_to_me.common.Result;
import com.github.listen_to_me.domain.vo.AudioOrderVO;
import com.github.listen_to_me.service.IAudioOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@AllArgsConstructor
@RequestMapping("/user/audio")
@RestController
@Tag(name = "音频订单管理", description = "包含订单创建、查询订单等接口")
public class AudioOrderController {
    private final IAudioOrderService audioOrderService;
    @PostMapping("/{audioId}/purchase")
    @Operation(summary = "购买音频")
    public Result<AudioOrderVO> purchaseAudio(@PathVariable Long audioId) {
        return Result.success(audioOrderService.purchaseAudio(audioId));
    }
}
