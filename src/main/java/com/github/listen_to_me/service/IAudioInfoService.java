package com.github.listen_to_me.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.listen_to_me.domain.dto.AudioDTO;
import com.github.listen_to_me.domain.dto.AudioUpdateDTO;
import com.github.listen_to_me.domain.dto.CreatorAudioDetailVO;
import com.github.listen_to_me.domain.entity.AudioInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.listen_to_me.domain.query.AudioSearchQuery;
import com.github.listen_to_me.domain.query.FavoriteQuery;
import com.github.listen_to_me.domain.query.PageQuery;
import com.github.listen_to_me.domain.vo.*;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author kun
 * @since 2026-03-24
 */
public interface IAudioInfoService extends IService<AudioInfo> {

    IPage<AudioVO> getFavoriteAudioPage(FavoriteQuery favoriteQuery);

    String uploadAudio(MultipartFile audioFile) throws Exception;

    String uploadCover(MultipartFile coverFile) throws Exception;

    AudioPublishVO saveAudio(AudioDTO audioDTO);

    void MoveAudioToOnline(Long audioId) throws Exception;

    IPage<CreatorAudioVO> getAudioPage(PageQuery pageQuery);

    CreatorAudioDetailVO getCreatorAudioDetail(Long id);

    AudioStatusVO getAudioStatus(Long id);

    void updateAudio(AudioUpdateDTO audioUpdateDTO);

    void removeAudioInfo(Long id);

    List<AudioVO> getHotList();

    IPage<AudioVO> searchAudio(AudioSearchQuery audioSearchQuery);

    String getStreamSign(Long audioId);

    AudioDetailVO getAudioDetail(Long id);
}
