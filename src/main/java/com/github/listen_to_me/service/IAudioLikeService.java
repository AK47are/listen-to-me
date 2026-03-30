package com.github.listen_to_me.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.listen_to_me.domain.dto.LikeActionDTO;
import com.github.listen_to_me.domain.entity.AudioLike;
import com.github.listen_to_me.domain.query.PageQuery;
import com.github.listen_to_me.domain.vo.AudioVO;

public interface IAudioLikeService extends IService<AudioLike> {
    void modifyAudioLike(LikeActionDTO likeActionDTO);

    IPage<AudioVO> getLikePage(PageQuery pageQuery);
}
