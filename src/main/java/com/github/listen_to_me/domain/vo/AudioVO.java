package com.github.listen_to_me.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class AudioVO {
    private Long id;
    private String title;
    private String coverUrl;
    private BigDecimal price;
    private Integer trialDuration;
    private Integer viewCount;
    private List<String> tags = new ArrayList<>();
    private LocalDateTime createTime;
    private UserVO creator;
}
