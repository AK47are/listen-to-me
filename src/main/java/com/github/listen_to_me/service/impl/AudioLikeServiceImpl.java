package com.github.listen_to_me.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.listen_to_me.common.exception.BaseException;
import com.github.listen_to_me.common.exception.ConflictException;
import com.github.listen_to_me.common.util.MinioUtils;
import com.github.listen_to_me.domain.entity.AudioLike;
import com.github.listen_to_me.domain.query.PageQuery;
import com.github.listen_to_me.domain.vo.AudioVO;
import com.github.listen_to_me.mapper.AudioLikeMapper;
import com.github.listen_to_me.mapper.AudioVOMapper;
import com.github.listen_to_me.service.IAudioLikeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AudioLikeServiceImpl extends ServiceImpl<AudioLikeMapper, AudioLike> implements IAudioLikeService {

    private final AudioLikeMapper audioLikeMapper;
    private final AudioVOMapper audioVOMapper;

    @Override
    public void likeAudio(Long userId, Long audioId) {
        Wrapper<AudioLike> wrapper = Wrappers.lambdaQuery(AudioLike.class)
                .eq(AudioLike::getUserId, userId)
                .eq(AudioLike::getAudioId, audioId);
        if (audioLikeMapper.selectOne(wrapper) != null) {
            throw new ConflictException("音频已喜欢");
        }
        AudioLike audioLike = new AudioLike();
        audioLike.setUserId(userId);
        audioLike.setAudioId(audioId);
        audioLikeMapper.insert(audioLike);
    }

    @Override
    public void unlikeAudio(Long userId, Long audioId) {
        Wrapper<AudioLike> wrapper = Wrappers.lambdaQuery(AudioLike.class)
                .eq(AudioLike::getUserId, userId)
                .eq(AudioLike::getAudioId, audioId);
        if (audioLikeMapper.selectOne(wrapper) == null) {
            throw new BaseException(404, "喜欢记录不存在");
        }
        audioLikeMapper.delete(wrapper);
    }

    @Override
    public IPage<AudioVO> getLikePage(Long userId, PageQuery pageQuery) {
        Page<AudioVO> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        IPage<AudioVO> result = audioVOMapper.selectByLikeUserId(page, userId);

        result.getRecords().forEach(vo -> vo.setCoverUrl(MinioUtils.getPresignedUrl(vo.getCoverUrl())));

        return result;
    }
}
