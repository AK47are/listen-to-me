package com.github.listen_to_me.controller.admin;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.listen_to_me.common.Result;
import com.github.listen_to_me.domain.dto.RefundAuditDTO;
import com.github.listen_to_me.domain.query.RefundPageQuery;
import com.github.listen_to_me.domain.vo.RefundApplyVO;
import com.github.listen_to_me.service.IRefundApplyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/admin/consult/refund")
@RequiredArgsConstructor
@Tag(name = "退款管理")
public class RefundController {

    private final IRefundApplyService refundApplyService;

    @GetMapping("/page")
    @Operation(summary = "退款申请分页")
    public Result<IPage<RefundApplyVO>> getRefundApplyPage(@ParameterObject RefundPageQuery query) {
        return Result.success(refundApplyService.getRefundApplyPage(query));
    }

    @PutMapping("/audio")
    @Operation(summary = "审核退款")
    public Result<Void> auditRefund(@RequestBody RefundAuditDTO refundAuditDTO) {
        refundApplyService.auditRefund(refundAuditDTO);
        return Result.success();
    }

}
