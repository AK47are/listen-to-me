package com.github.listen_to_me.controller.user;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.listen_to_me.common.Result;
import com.github.listen_to_me.domain.dto.ConsultDTO;
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
}
