package com.github.listen_to_me.common.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum ConsultOrderStatus implements DescribedEnum<String> {
    PENDING_CONFIRM("PENDING_CONFIRM", "待确认"),
    CONFIRMED("CONFIRMED", "已确认"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消"),
    REFUND_PENDING("REFUND_PENDING", "退款中"),
    REFUNDED("REFUNDED", "已退款");

    public static final String SCHEMA_DESC = "订单状态: PENDING_CONFIRM, CONFIRMED, COMPLETED, CANCELLED, REFUND_PENDING, REFUNDED";

    @EnumValue
    private final String code;
    private final String label;

    ConsultOrderStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
