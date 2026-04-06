package com.github.listen_to_me.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class AudioOrderDetailVO {
    private String orderSn;
    private String audioTitle;
    private String coverUrl;
    private BigDecimal payAmount;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
}
