package com.github.listen_to_me.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.listen_to_me.domain.entity.AudioInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.listen_to_me.domain.query.FavoriteQuery;
import com.github.listen_to_me.domain.vo.AudioVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author kun
 * @since 2026-03-24
 */
public interface IAudioInfoService extends IService<AudioInfo> {

    IPage<AudioVO> getFavoriteAudioPage(FavoriteQuery favoriteQuery);

    String uploadAudio(MultipartFile audioFile) throws Exception;
}
