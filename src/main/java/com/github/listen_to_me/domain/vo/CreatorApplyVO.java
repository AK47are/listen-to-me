package com.github.listen_to_me.domain.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CreatorApplyVO {
    private String status;
    private String reason;
    private LocalDateTime applyTime;
}
