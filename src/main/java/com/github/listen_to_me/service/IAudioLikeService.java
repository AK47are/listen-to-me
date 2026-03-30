package com.github.listen_to_me.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.listen_to_me.domain.dto.LikeActionDTO;
import com.github.listen_to_me.domain.entity.AudioLike;

public interface IAudioLikeService extends IService<AudioLike> {
    void modifyAudioLike(LikeActionDTO likeActionDTO);
}
