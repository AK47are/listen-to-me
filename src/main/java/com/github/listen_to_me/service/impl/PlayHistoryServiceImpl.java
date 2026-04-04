package com.github.listen_to_me.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.listen_to_me.common.enumeration.RedisKey;
import com.github.listen_to_me.common.exception.BaseException;
import com.github.listen_to_me.common.util.MinioUtils;
import com.github.listen_to_me.common.util.RedisUtils;
import com.github.listen_to_me.common.util.SecurityUtils;
import com.github.listen_to_me.domain.dto.HistoryProgressDTO;
import com.github.listen_to_me.domain.entity.AudioInfo;
import com.github.listen_to_me.domain.entity.PlayHistory;
import com.github.listen_to_me.domain.query.PageQuery;
import com.github.listen_to_me.domain.vo.AudioVO;
import com.github.listen_to_me.mapper.AudioInfoMapper;
import com.github.listen_to_me.mapper.AudioVOMapper;
import com.github.listen_to_me.mapper.PlayHistoryMapper;
import com.github.listen_to_me.service.IPlayHistoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PlayHistoryServiceImpl extends ServiceImpl<PlayHistoryMapper, PlayHistory> implements IPlayHistoryService {

    private final PlayHistoryMapper playHistoryMapper;
    private final AudioInfoMapper audioInfoMapper;
    private final AudioVOMapper audioVOMapper;

    @Override
    public void addPlayHistory(HistoryProgressDTO historyProgressDTO) {
        Long currId = SecurityUtils.getCurrentUserId();
        Long audioId = historyProgressDTO.getAudioId();
        AudioInfo audioInfo = audioInfoMapper.selectById(audioId);
        if (audioInfo == null) {
            throw new BaseException(404, "音频不存在");
        }
        if (audioInfo.getDuration() < historyProgressDTO.getLastPosition()) {
            throw new BaseException(400, "播放进度不能大于音频时长");
        }
        PlayHistory history = new PlayHistory();
        history.setAudioId(audioId);
        history.setUserId(currId);
        history.setLastPosition(historyProgressDTO.getLastPosition());
        String historySuffix = currId.toString() + ":" + audioId.toString();
        RedisUtils.set(RedisKey.USER_HISTORY, historySuffix, historyProgressDTO.getLastPosition());
        playHistoryMapper.insertOrUpdate(history);
    }

    @Override
    public IPage<AudioVO> getHistoryPage(PageQuery pageQuery) {
        Long userId = SecurityUtils.getCurrentUserId();
        Page<AudioVO> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        IPage<AudioVO> result = audioVOMapper.selectHistoryByUserId(page, userId);

        result.getRecords().forEach(vo -> vo.setCoverUrl(MinioUtils.getPresignedUrl(vo.getCoverUrl())));

        return result;
    }

    @Override
    public Integer findPlayHistory(Long audioId) {
        Long currId = SecurityUtils.getCurrentUserId();
        String historySuffix = currId.toString() + ":" + audioId.toString();
        Integer lastPosition = RedisUtils.get(RedisKey.USER_HISTORY, historySuffix);
        if (lastPosition != null) {
            return lastPosition;
        }
        // TODO 后续改成异步 Redis 方式，让数据库定时向 Redis 读取更新数据
        PlayHistory history = playHistoryMapper.selectOne(Wrappers.lambdaQuery(PlayHistory.class)
                .eq(PlayHistory::getUserId, currId)
                .eq(PlayHistory::getAudioId, audioId));
        if (history == null) {
            return null;
        }
        RedisUtils.set(RedisKey.USER_HISTORY, historySuffix, history.getLastPosition());
        return history.getLastPosition();
    }
}
