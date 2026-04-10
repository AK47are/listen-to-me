package com.github.listen_to_me.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.listen_to_me.domain.dto.AudioAuditDTO;
import com.github.listen_to_me.domain.dto.AudioDTO;
import com.github.listen_to_me.domain.dto.AudioUpdateDTO;
import com.github.listen_to_me.domain.dto.CreatorAudioDetailVO;
import com.github.listen_to_me.domain.entity.AudioInfo;
import com.github.listen_to_me.domain.query.AudioSearchQuery;
import com.github.listen_to_me.domain.query.AuditQuery;
import com.github.listen_to_me.domain.query.FavoriteQuery;
import com.github.listen_to_me.domain.query.PageQuery;
import com.github.listen_to_me.domain.vo.AudioDetailVO;
import com.github.listen_to_me.domain.vo.AudioPublishVO;
import com.github.listen_to_me.domain.vo.AudioStatusVO;
import com.github.listen_to_me.domain.vo.AudioVO;
import com.github.listen_to_me.domain.vo.AuditAudioVO;
import com.github.listen_to_me.domain.vo.CreatorAudioVO;

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

    IPage<CreatorAudioVO> getAudioPage(Long userId, PageQuery pageQuery);

    CreatorAudioDetailVO getCreatorAudioDetail(Long creatorId, Long audioId);

    AudioStatusVO getAudioStatus(Long id);

    void updateAudio(AudioUpdateDTO audioUpdateDTO);

    void removeAudioInfo(Long id);

    List<AudioVO> getHotList();

    IPage<AudioVO> searchAudio(AudioSearchQuery audioSearchQuery);

    String getStreamSign(Long audioId);

    AudioDetailVO getAudioDetail(Long userId, Long audioId);

    IPage<AuditAudioVO> getAuditAudioPage(AuditQuery auditQuery);

    void auditAudio(AudioAuditDTO audioAuditDTO);

    /**
     * 分页查询指定创作者的音频作品
     *
     * @param creatorId 创作者ID
     * @param pageQuery 分页参数
     * @return 音频分页列表
     */
    IPage<AudioVO> getCreatorAudioPage(Long creatorId, PageQuery pageQuery);

    IPage<AudioVO> getRecommendList(PageQuery pageQuery);
}
