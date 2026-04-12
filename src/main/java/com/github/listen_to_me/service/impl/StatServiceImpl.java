package com.github.listen_to_me.service.impl;

import org.springframework.stereotype.Service;

import com.github.listen_to_me.domain.query.DashboardQuery;
import com.github.listen_to_me.domain.vo.DashboardVO;
import com.github.listen_to_me.mapper.StatMapper;
import com.github.listen_to_me.service.IStatService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatServiceImpl implements IStatService {

    private final StatMapper statMapper;

    @Override
    public DashboardVO getDashboard(DashboardQuery query) {
        log.debug("获取全站数据大盘 - startDate: {}, endDate: {}", query.getStartDate(), query.getEndDate());
        return statMapper.selectDashboardStats(query);
    }
}
