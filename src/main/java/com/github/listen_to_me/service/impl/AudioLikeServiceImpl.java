package com.github.listen_to_me.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.listen_to_me.common.exception.BaseException;
import com.github.listen_to_me.common.exception.ConflictException;
import com.github.listen_to_me.common.util.SecurityUtils;
import com.github.listen_to_me.domain.dto.LikeActionDTO;
import com.github.listen_to_me.domain.entity.AudioLike;
import com.github.listen_to_me.mapper.AudioLikeMapper;
import com.github.listen_to_me.service.IAudioLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AudioLikeServiceImpl extends ServiceImpl<AudioLikeMapper, AudioLike> implements IAudioLikeService {
    private final AudioLikeMapper audioLikeMapper;

    @Override
    public void modifyAudioLike(LikeActionDTO likeActionDTO) {
        Long currId = SecurityUtils.getCurrentUserId();
        Wrapper<AudioLike> wrapper = Wrappers.lambdaQuery(AudioLike.class)
                .eq(AudioLike::getUserId, currId)
                .eq(AudioLike::getAudioId, likeActionDTO.getAudioId());
        if(likeActionDTO.getAction().equals("LIKE")){
            if(audioLikeMapper.selectOne(wrapper) != null ){
                throw new ConflictException("音频已喜欢");
            }
            AudioLike audioLike = new AudioLike();
            audioLike.setUserId(currId);
            audioLike.setAudioId(likeActionDTO.getAudioId());
            audioLikeMapper.insert(audioLike);
        }else if(likeActionDTO.getAction().equals("UNLIKE")){
            if(audioLikeMapper.selectOne(wrapper) == null){
                throw new BaseException(404,"喜欢记录不存在");
            }
            audioLikeMapper.delete(wrapper);
        }else {
            throw new BaseException("前端操作错误");
        }
    }
}
