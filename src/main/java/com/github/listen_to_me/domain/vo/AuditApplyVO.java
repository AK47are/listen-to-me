package com.github.listen_to_me.domain.vo;

import lombok.Data;

@Data
public class AuditApplyVO {
    private Long id;
    private String realName;
    private String phone;
    private String intro;
    private String attachment;
    private String applyTime;
}
