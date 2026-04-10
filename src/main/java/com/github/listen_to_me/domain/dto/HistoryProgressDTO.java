package com.github.listen_to_me.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class HistoryProgressDTO {
    @NotNull(message = "音频ID不能为空")
    private Long audioId;

    @PositiveOrZero(message = "播放进度不能为负数")
    @NotNull(message = "播放进度不能为空")
    private Integer lastPosition;
}
