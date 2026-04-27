package com.github.listen_to_me.service;

import java.util.Set;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.github.listen_to_me.common.enumeration.RedisKey;
import com.github.listen_to_me.common.util.RedisUtils;
import com.github.listen_to_me.domain.entity.PlayHistory;
import com.github.listen_to_me.mapper.PlayHistoryMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayHistorySyncService {

    private final PlayHistoryMapper playHistoryMapper;

    @Scheduled(fixedDelayString = "${play-history.sync.interval:30000}")
    public void syncDirtyRecords() {
        Set<Object> dirtyEntries = RedisUtils.getSetMembers(RedisKey.USER_HISTORY_DIRTY, "");
        if (dirtyEntries == null || dirtyEntries.isEmpty()) {
            return;
        }

        int success = 0;
        int fail = 0;
        for (Object entry : dirtyEntries) {
            String suffix = entry.toString();
            Integer lastPosition = RedisUtils.get(RedisKey.USER_HISTORY, suffix);
            if (lastPosition == null) {
                RedisUtils.removeFromSet(RedisKey.USER_HISTORY_DIRTY, "", suffix);
                continue;
            }
            try {
                String[] parts = suffix.split(":");
                PlayHistory history = new PlayHistory();
                history.setUserId(Long.valueOf(parts[0]));
                history.setAudioId(Long.valueOf(parts[1]));
                history.setLastPosition(lastPosition);
                playHistoryMapper.insertOrUpdate(history);
                RedisUtils.removeFromSet(RedisKey.USER_HISTORY_DIRTY, "", suffix);
                success++;
            } catch (Exception e) {
                log.error("同步播放历史失败: {}", suffix, e);
                fail++;
            }
        }
        if (fail > 0) {
            log.warn("播放历史同步完成: {} 成功, {} 失败", success, fail);
        }
    }
}
