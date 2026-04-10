package com.github.listen_to_me.controller.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.listen_to_me.common.Result;
import com.github.listen_to_me.domain.dto.CreatorApplyDTO;
import com.github.listen_to_me.domain.vo.CreatorApplyVO;
import com.github.listen_to_me.service.CreatorApplyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "创作者申请接口")
@RestController
@RequestMapping("/user/creator/apply")
@RequiredArgsConstructor
public class CreatorApplyController {

    private final CreatorApplyService creatorApplyService;

    @Operation(summary = "提交创作者申请")
    @PostMapping
    public Result<Void> applyCreator(@Valid @RequestBody CreatorApplyDTO creatorApplyDTO) {
        creatorApplyService.addCreatorApply(creatorApplyDTO);
        return Result.success();
    }

    @Operation(summary = "查询创作者申请状态")
    @GetMapping("/status")
    public Result<CreatorApplyVO> getApplyStatus() {
        return Result.success(creatorApplyService.findApplyStatus());
    }
}
