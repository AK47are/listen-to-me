package com.github.listen_to_me.domain.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreatorVO {
    private Long creatorId;
    private String nickname;
    private String avatar;
    private String intro;
    private Long consultCount;
    private Long audioCount;
    private Long fansCount;
    private BigDecimal minPrice;
    private Boolean isFollowing;
}
