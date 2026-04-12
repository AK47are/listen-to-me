package com.github.listen_to_me.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.github.listen_to_me.domain.query.DashboardQuery;
import com.github.listen_to_me.domain.vo.DashboardVO;

@Mapper
public interface StatMapper {
    DashboardVO selectDashboardStats(DashboardQuery query);
}
