package com.github.listen_to_me.controller.admin;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.listen_to_me.common.Result;
import com.github.listen_to_me.domain.query.DashboardQuery;
import com.github.listen_to_me.domain.vo.DashboardVO;
import com.github.listen_to_me.service.IStatService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/admin/stat")
@AllArgsConstructor
@Tag(name = "数据统计", description = "管理员数据统计接口")
public class StatController {

    private final IStatService statService;

    @GetMapping("/dashboard")
    @Operation(summary = "全站数据大盘")
    public Result<DashboardVO> getDashboard(@Valid @ParameterObject DashboardQuery query) {
        return Result.success(statService.getDashboard(query));
    }
}
