package com.github.listen_to_me.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AudioOrderVO {
    private String orderSn;
    private String audioTitle;
    private String coverUrl;
    private BigDecimal payAmount;
    private String status;
    private LocalDateTime createTime;
}
