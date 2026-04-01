package com.github.listen_to_me.controller.user;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.listen_to_me.common.Result;
import com.github.listen_to_me.domain.dto.ConsultDTO;
import com.github.listen_to_me.domain.dto.RefundApplyDTO;
import com.github.listen_to_me.domain.query.ConsultPageQuery;
import com.github.listen_to_me.domain.vo.ConsultOrderVO;
import com.github.listen_to_me.service.IConsultOrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@Tag(name = "预约管理")
@RequiredArgsConstructor
@RequestMapping("/user/consult")
public class ConsultController {

    private final IConsultOrderService consultOrderService;

    @PostMapping
    @Operation(summary = "发起预约")
    public Result<ConsultOrderVO> saveConsult(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ConsultDTO consultDTO) {
        ConsultOrderVO vo = consultOrderService.saveConsult(userId, consultDTO);
        return Result.success(vo);
    }

    @GetMapping("/page")
    @Operation(summary = "查询我的预约")
    public Result<IPage<ConsultOrderVO>> getMyConsultPage(
            @AuthenticationPrincipal Long userId,
            @ParameterObject ConsultPageQuery query) {
        return Result.success(consultOrderService.getUserConsultPage(userId, query));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消预约")
    public Result<Void> cancelConsult(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        consultOrderService.cancelConsult(userId, id);
        return Result.success();
    }

    @PutMapping("/consult/{id}/refund")
    @Operation(summary = "申请退款")
    public Result<Void> applyRefund(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @Valid @RequestBody RefundApplyDTO refundApplyDTO) {
        consultOrderService.applyRefund(userId, id, refundApplyDTO);
        return Result.success();
    }
}
