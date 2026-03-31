package com.github.listen_to_me.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.listen_to_me.domain.dto.ConsultDTO;
import com.github.listen_to_me.domain.entity.ConsultOrder;
import com.github.listen_to_me.domain.vo.ConsultOrderVO;

public interface IConsultOrderService extends IService<ConsultOrder> {

    /**
     * 发起预约
     * 
     * @param userId     用户ID
     * @param consultDTO 预约请求
     * @return 预约订单VO
     */
    ConsultOrderVO saveConsult(Long userId, ConsultDTO consultDTO);
}
