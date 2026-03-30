package com.github.listen_to_me.controller.creator;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.listen_to_me.common.Result;
import com.github.listen_to_me.domain.dto.SlotDTO;
import com.github.listen_to_me.service.IConsultSlotService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/creator")
@RequiredArgsConstructor
@Tag(name = "时间槽管理")
public class CreatorSlotController {

    private final IConsultSlotService slotService;

    @PostMapping("/slots")
    @Operation(summary = "批量生成时间槽")
    public Result<Void> saveSlotBatch(@Valid @RequestBody List<SlotDTO> slotDTOList) {
        log.debug("批量生成时间槽 - 请求数量: {}", slotDTOList.size());
        slotService.saveSlotBatch(slotDTOList);
        return Result.success();
    }
}
