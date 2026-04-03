package com.github.listen_to_me.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreatorApplyVO {
    private String status;
    private String reason;
    private LocalDateTime applyTime;
}
