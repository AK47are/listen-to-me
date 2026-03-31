package com.github.listen_to_me.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "创作者音频信息列表")
public class CreatorAudioVO {
    private  Long id;
    private String title;
    private String coverUrl;
    private Long duration;
    private Double price;
    private Boolean isPaid;
    private String visibility;
    private  String status;
    private Integer playCount;
    private LocalDateTime createTime;
}
