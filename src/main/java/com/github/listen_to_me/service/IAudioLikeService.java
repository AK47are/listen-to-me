package com.github.listen_to_me.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.listen_to_me.domain.entity.AudioLike;
import com.github.listen_to_me.domain.query.PageQuery;
import com.github.listen_to_me.domain.vo.AudioVO;

public interface IAudioLikeService extends IService<AudioLike> {
    void likeAudio(Long userId, Long audioId);

    void unlikeAudio(Long userId, Long audioId);

    IPage<AudioVO> getLikePage(Long userId, PageQuery pageQuery);
}
