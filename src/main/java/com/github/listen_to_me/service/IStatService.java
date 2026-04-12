package com.github.listen_to_me.service;

import com.github.listen_to_me.domain.query.DashboardQuery;
import com.github.listen_to_me.domain.vo.DashboardVO;

public interface IStatService {

    // 获取全站大盘数据
    DashboardVO getDashboard(DashboardQuery query);
}
