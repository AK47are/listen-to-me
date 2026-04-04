package com.github.listen_to_me.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.github.listen_to_me.common.enumeration.RedisKey;
import com.github.listen_to_me.common.util.RedisUtils;
import com.github.listen_to_me.domain.dto.AudioStatsDTO;
import com.github.listen_to_me.mapper.AudioInfoMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotRankService {

    private static final RedisKey HOT_RANK_KEY = RedisKey.HOT_AUDIO_RANK;

    private final AudioInfoMapper audioInfoMapper;

    @Scheduled(cron = "0 0 * * * *")
    public void refreshHotRank() {
        log.info("开始刷新热门音频排名 - 时间: {}", LocalDateTime.now());

        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        List<AudioStatsDTO> statsList = audioInfoMapper.selectAudioStatsForHotRank(oneMonthAgo);
        log.debug("查询到音频数量: {}", statsList.size());

        Map<Long, Double> scoreMap = statsList.stream()
                .collect(Collectors.toMap(
                        AudioStatsDTO::getId,
                        stats -> calculate(
                                stats.getPlayCount(),
                                stats.getLikeCount(),
                                stats.getCollectCount(),
                                stats.getCommentCount())));

        batchUpdate(scoreMap);
        log.info("热门音频排名刷新完成 - 时间: {}", LocalDateTime.now());
    }

    /**
     * 批量更新热榜（全量替换）
     * <p>
     * 先清空原有热榜，再批量写入新数据
     * 用于定时任务全量计算场景
     * </p>
     *
     * @param scores 音频ID与热度分的映射，key为音频ID，value为热度分
     */
    public void batchUpdate(Map<Long, Double> scores) {
        RedisUtils.deleteZSet(HOT_RANK_KEY, "");
        scores.forEach((audioId, score) -> {
            RedisUtils.addToZSet(HOT_RANK_KEY, "", audioId.toString(), score);
        });
        log.debug("批量更新热榜完成 - 数量: {}", scores.size());
    }

    /**
     * 获取热榜前N名（仅返回音频ID）
     *
     * @param limit 需要获取的数量（如10表示TOP10）
     * @return 音频ID集合，按热度分从高到低排序
     */
    public Set<Object> getTopN(int limit) {
        return RedisUtils.getZSetReverseRange(HOT_RANK_KEY, "", 0, limit - 1);
    }

    /**
     * 根据音频统计数据计算热度分
     *
     * @param playCount    播放量
     * @param likeCount    点赞数
     * @param collectCount 收藏数
     * @param commentCount 评论数
     * @return 热度分
     */
    public double calculate(long playCount, long likeCount, long collectCount, long commentCount) {
        return 0.5 * playCount + collectCount + likeCount * 1.2 + commentCount * 1.5;
    }
}
