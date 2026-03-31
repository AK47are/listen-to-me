package com.github.listen_to_me.controller.creator;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.listen_to_me.common.Result;
import com.github.listen_to_me.domain.dto.SlotDTO;
import com.github.listen_to_me.domain.query.SlotPageQuery;
import com.github.listen_to_me.domain.vo.SlotVO;
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
public class SlotController {

    private final IConsultSlotService slotService;

    @PostMapping("/slots")
    @Operation(summary = "批量生成时间槽")
    public Result<Void> saveSlotBatch(@Valid @RequestBody List<SlotDTO> slotDTOList) {
        log.debug("批量生成时间槽 - 请求数量: {}", slotDTOList.size());
        slotService.saveSlotBatch(slotDTOList);
        return Result.success();
    }

    @GetMapping("/slots/page")
    @Operation(summary = "分页查询时间槽")
    public Result<IPage<SlotVO>> getSlotPage(@ParameterObject SlotPageQuery query) {
        return Result.success(slotService.getCreatorSlotPage(query));
    }

    @PutMapping("/slots/{id}")
    @Operation(summary = "修改时间槽状态")
    public Result<Void> updateSlotStatus(@PathVariable Long id, @RequestParam String status) {
        log.debug("修改时间槽状态 - ID: {}, 状态: {}", id, status);
        slotService.updateSlotStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/slots/{id}")
    @Operation(summary = "删除时间槽")
    public Result<Void> removeSlot(@PathVariable Long id) {
        log.debug("删除时间槽 - ID: {}", id);
        slotService.removeSlot(id);
        return Result.success();
    }
}
