package com.github.listen_to_me.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.listen_to_me.domain.entity.AudioInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.listen_to_me.domain.query.AudioQuery;
import com.github.listen_to_me.domain.vo.AudioVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author kun
 * @since 2026-03-24
 */
public interface IAudioInfoService extends IService<AudioInfo> {
    Page<AudioVO> findAudioPage(AudioQuery audioQuery);
}
