package com.github.listen_to_me.domain.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class FansVO {
    private Long userId;
    private String nickname;
    private String avatar;
    private LocalDateTime followTime;
}
