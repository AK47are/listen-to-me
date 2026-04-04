package com.github.listen_to_me.domain.dto;

import lombok.Data;

/**
 * 音频统计数据传输对象
 * <p>
 * 用于热榜计算等场景，仅包含音频ID和各项统计数据
 * 避免查询整个 AudioInfo 实体，减少数据传输量
 * </p>
 */
@Data
public class AudioStatsDTO {

    private Long id;
    private Long playCount;
    private Long likeCount;
    private Long collectCount;
    private Long commentCount;
}
