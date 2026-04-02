package com.github.listen_to_me.controller.creator;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.listen_to_me.common.Result;
import com.github.listen_to_me.domain.query.ConsultPageQuery;
import com.github.listen_to_me.domain.vo.ConsultOrderVO;
import com.github.listen_to_me.service.IConsultOrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/creator/consult")
@Tag(name = "预约订单管理")
@RestController("creatorConsultController")
public class ConsultController {

    private final IConsultOrderService consultOrderService;

    @GetMapping("/page")
    @Operation(summary = "分页查询预约订单")
    public Result<IPage<ConsultOrderVO>> getConsultPage(
            @AuthenticationPrincipal Long creatorId,
            @ParameterObject ConsultPageQuery query) {
        return Result.success(consultOrderService.getCreatorConsultPage(creatorId, query));
    }

    @PutMapping("/{id}/confirm")
    @Operation(summary = "确认预约")
    public Result<Void> confirmConsult(
            @AuthenticationPrincipal Long creatorId,
            @PathVariable Long id,
            @RequestParam(required = false) String address) {
        consultOrderService.confirmConsult(creatorId, id, address);
        return Result.success();
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "拒绝预约")
    public Result<Void> rejectConsult(
            @AuthenticationPrincipal Long creatorId,
            @PathVariable Long id) {
        consultOrderService.rejectConsult(creatorId, id);
        return Result.success();
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "标记完成")
    public Result<Void> completeConsult(
            @AuthenticationPrincipal Long creatorId,
            @PathVariable Long id) {
        consultOrderService.completeConsult(creatorId, id);
        return Result.success();
    }
}
