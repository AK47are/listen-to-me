package com.github.listen_to_me.domain.vo;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class DashboardVO {
    // 累计指标
    private Long totalUsers;
    private Long totalCreators;
    private Long totalAudios;
    private Long approvedAudios;
    private Long totalPlays;
    private BigDecimal totalAudioSales;
    private BigDecimal totalRecharge;
    private BigDecimal totalConsultSales;
    private Long totalCollects;
    private Long totalLikes;
    private Long totalComments;
    private Long newUsers;
    private Long newCreators;
    private Long newAudios;
    private Long newApprovedAudios;
    private Long newCollects;
    private Long newLikes;
    private Long newComments;
}
