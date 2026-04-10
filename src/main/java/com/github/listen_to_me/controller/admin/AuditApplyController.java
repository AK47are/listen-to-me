package com.github.listen_to_me.controller.admin;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.listen_to_me.common.Result;
import com.github.listen_to_me.domain.dto.ApplyAuditDTO;
import com.github.listen_to_me.domain.query.AuditQuery;
import com.github.listen_to_me.domain.vo.AuditApplyVO;
import com.github.listen_to_me.service.CreatorApplyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "审核创作者申请")
@RequestMapping("/admin/creator/apply/audit")
@RestController
@RequiredArgsConstructor
public class AuditApplyController {

    private final CreatorApplyService creatorApplyService;

    @Operation(summary = "创作者申请分页")
    @GetMapping("/page")
    public Result<IPage<AuditApplyVO>> getAuditApplyPage(@ParameterObject AuditQuery query) {
        return Result.success(creatorApplyService.findAuditApplyPage(query));
    }

    @Operation(summary = "执行申请审核")
    @PutMapping
    public Result<Void> auditApply(@RequestBody ApplyAuditDTO applyAuditDTO) {
        creatorApplyService.auditApply(applyAuditDTO);
        return Result.success();
    }
}
