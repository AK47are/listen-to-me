package com.github.listen_to_me.controller.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.listen_to_me.common.Result;
import com.github.listen_to_me.domain.query.PageQuery;
import com.github.listen_to_me.domain.vo.AudioOrderDetailVO;
import com.github.listen_to_me.domain.vo.AudioOrderVO;
import com.github.listen_to_me.service.IAudioOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/order/{sn}")
    @Operation(summary = "查询音频订单")
    public Result<AudioOrderDetailVO> queryAudioOrder(@PathVariable String sn) {
        return Result.success(audioOrderService.queryAudioOrderDetail(sn));
    }

    @GetMapping("/order/page")
    @Operation(summary = "查询音频订单列表")
    public Result<IPage<AudioOrderVO>> queryAudioOrderPage(@ParameterObject PageQuery query) {
        return Result.success(audioOrderService.queryAudioOrderPage(query));
    }
}
