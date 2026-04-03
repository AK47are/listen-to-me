package com.github.listen_to_me.domain.dto;

import lombok.Data;

@Data
public class ApplyAuditDTO {
    private Long applyId;
    private String status;
    private String rejectReason;
}
