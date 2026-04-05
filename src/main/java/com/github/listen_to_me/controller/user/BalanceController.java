package com.github.listen_to_me.controller.user;

import com.github.listen_to_me.domain.dto.RechargeResultDTO;
import com.github.listen_to_me.domain.vo.RechargeResultVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.github.listen_to_me.common.Result;
import com.github.listen_to_me.domain.vo.BalanceVO;
import com.github.listen_to_me.service.ISysUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "虚拟币管理")
public class BalanceController {

    private final ISysUserService sysUserService;

    @GetMapping("/balance")
    @Operation(summary = "查询余额")
    public Result<BalanceVO> getBalance(@AuthenticationPrincipal Long userId) {
        log.debug("查询余额 - 用户ID: {}", userId);
        return Result.success(sysUserService.getBalanceStats(userId));
    }

    @PostMapping("/recharge")
    @Operation(summary = "充值")
    public Result<RechargeResultVO> recharge(@Valid @RequestBody RechargeResultDTO rechargeResultDTO) throws Exception {
        return Result.success(sysUserService.recharge(rechargeResultDTO));
    }

}
