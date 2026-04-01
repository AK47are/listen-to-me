package com.github.listen_to_me.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.listen_to_me.domain.dto.ConsultDTO;
import com.github.listen_to_me.domain.dto.RefundApplyDTO;
import com.github.listen_to_me.domain.entity.ConsultOrder;
import com.github.listen_to_me.domain.query.ConsultPageQuery;
import com.github.listen_to_me.domain.vo.ConsultOrderVO;

public interface IConsultOrderService extends IService<ConsultOrder> {

    /**
     * 用户发起预约
     * 
     * @param userId     用户ID
     * @param consultDTO 预约请求
     * @return 预约订单VO
     */
    ConsultOrderVO saveConsult(Long userId, ConsultDTO consultDTO);

    /**
     * 分页查询指定用户的预约订单
     * 
     * @param userId 用户ID
     * @param query  分页查询条件
     * @return 分页结果
     */
    IPage<ConsultOrderVO> getUserConsultPage(Long userId, ConsultPageQuery query);

    /**
     * 用户取消预约
     * 
     * @param userId  用户ID
     * @param orderId 订单ID
     */
    void cancelConsult(Long userId, Long orderId);

    /**
     * 用户申请退款
     * 
     * @param userId         用户ID
     * @param orderId        订单ID
     * @param refundApplyDTO 退款申请参数
     */
    void applyRefund(Long userId, Long orderId, RefundApplyDTO refundApplyDTO);
}
