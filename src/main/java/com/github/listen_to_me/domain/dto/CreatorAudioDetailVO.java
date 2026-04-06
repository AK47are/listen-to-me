package com.github.listen_to_me.domain.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "音频详情")
public class CreatorAudioDetailVO {
    private Long id;
    private String title;
    private String coverUrl;
    private String description;
    private BigDecimal price;
    private Boolean isPaid;
    private Integer trialDuration;
    private Integer duration;
    private String visibility;
    private String status;
    private Integer playCount;
    private Long likeCount;
    private Long collectCount;
    private LocalDateTime createTime;
    private Long transcriptId;
    private String transcript;
   }
