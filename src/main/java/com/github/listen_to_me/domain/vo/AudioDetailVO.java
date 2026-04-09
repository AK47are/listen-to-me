package com.github.listen_to_me.domain.vo;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class AudioDetailVO {

    private Long id;
    private String title;
    private String coverUrl;
    private Integer duration;
    private Integer trialDuration;
    private BigDecimal price;
    private Boolean isPaid;
    private String description;
    private Boolean isPurchased;
    private Boolean isLike;

    private CreatorInfo creator;
    private StatsInfo stats;
    private String transcript;
    private String summary;

    @Data
    public static class CreatorInfo {
        private Long id;
        private String nickname;
        private String avatar;
        private Boolean isFollow;
    }

    @Data
    public static class StatsInfo {
        private Long playCount;
        private Long likeCount;
        private Long collectCount;
    }
}
