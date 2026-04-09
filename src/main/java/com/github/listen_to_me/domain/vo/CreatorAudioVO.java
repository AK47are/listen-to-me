package com.github.listen_to_me.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "创作者音频信息列表")
public class CreatorAudioVO extends AudioVO {
    private String visibility;
    private String status;
    private String rejectReason;
}
