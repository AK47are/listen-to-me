package com.github.listen_to_me.controller.creator;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
