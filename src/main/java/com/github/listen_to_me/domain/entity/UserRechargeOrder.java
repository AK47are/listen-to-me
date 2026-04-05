package com.github.listen_to_me.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_recharge_order")
public class UserRechargeOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String rechargeSn;
    private Long userId;
    private Double rechargeAmount;
    private String payStatus;
    private String payChannel;
    private LocalDateTime payTime;
    private LocalDateTime createTime;

}
