package com.github.listen_to_me.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author kun
 * @since 2026-03-24
 */
@Getter
@Setter
@TableName("order_info")
public class OrderInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String orderSn;

    private Long userId;

    private Long audioId;

    private BigDecimal payAmount;

    /**
     * 0-待支付, 1-已支付, 2-已取消
     */
    private Integer payStatus;

    /**
     * alipay, wechat
     */
    private String payChannel;

    private LocalDateTime payTime;

    private LocalDateTime createTime;
}
