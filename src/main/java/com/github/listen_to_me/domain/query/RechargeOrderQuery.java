package com.github.listen_to_me.domain.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RechargeOrderQuery extends PageQuery {

    /**
     * 订单状态
     * 枚举值: PENDING / SUCCESS / FAILED
     * 默认值 null 查询所有订单
     */
    private String status ;
}
