package com.github.listen_to_me.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.listen_to_me.domain.dto.AudioAuditDTO;
import com.github.listen_to_me.domain.query.AudioSearchQuery;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.listen_to_me.domain.entity.AudioOrder;
import com.github.listen_to_me.domain.query.AuditQuery;
import com.github.listen_to_me.mapper.AudioOrderMapper;
import cn.hutool.core.bean.BeanUtil;
import com.github.listen_to_me.domain.entity.*;
import com.github.listen_to_me.domain.vo.*;
import com.github.listen_to_me.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.listen_to_me.common.enumeration.RedisKey;
import com.github.listen_to_me.common.exception.BaseException;
import com.github.listen_to_me.common.producer.AudioTranscodeProducer;
import com.github.listen_to_me.common.util.MinioUtils;
import com.github.listen_to_me.common.util.RedisUtils;
import com.github.listen_to_me.common.util.SecurityUtils;
import com.github.listen_to_me.domain.dto.AudioDTO;
import com.github.listen_to_me.domain.dto.AudioUpdateDTO;
import com.github.listen_to_me.domain.dto.CreatorAudioDetailVO;
import com.github.listen_to_me.domain.query.FavoriteQuery;
import com.github.listen_to_me.domain.query.PageQuery;
import com.github.listen_to_me.service.HotRankService;
import com.github.listen_to_me.service.IAudioInfoService;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.file.FileNameUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author kun
 * @since 2026-03-24
 */
@Service
@AllArgsConstructor
@Slf4j
public class AudioInfoServiceImpl extends ServiceImpl<AudioInfoMapper, AudioInfo> implements IAudioInfoService {

    // 注入生产者
    private final AudioTranscodeProducer audioTranscodeProducer;
    private final AudioInfoMapper audioInfoMapper;
    private final AudioVOMapper audioVOMapper;
    private final HotRankService hotRankService;
    private final AudioOrderMapper audioOrderMapper;
    private final PlayHistoryMapper playHistoryMapper;
    private final SysUserMapper sysUserMapper;
    private final AudioLikeMapper audioLikeMapper;
    private final AudioFolderRelationMapper audioFolderRelationMapper;

    @Override
    public IPage<AudioVO> getFavoriteAudioPage(FavoriteQuery favoriteQuery) {
        Page<AudioVO> page = new Page<>(favoriteQuery.getPageNum(), favoriteQuery.getPageSize());
        IPage<AudioVO> result = audioVOMapper.selectByFolderId(page, favoriteQuery.getFolderId());

        result.getRecords().forEach(vo -> vo.setCoverUrl(MinioUtils.getPresignedUrl(vo.getCoverUrl())));

        return result;
    }

    @Override
    public String uploadAudio(MultipartFile audioFile) throws Exception {
        // TODO: 上传音频时解析时长的设计有问题，应该在异步后解析再添加，性能更好
        String fileType = FileTypeUtil.getType(audioFile.getInputStream());
        if (fileType == null || !"mp3".equals(fileType)) {
            throw new BaseException(400, "仅支持上传 MP3 格式音频");
        }
        File tempFile = null;
        double duration = 0;
        try {
            tempFile = File.createTempFile("temp_audio_",
                    "." + FileNameUtil.getSuffix(audioFile.getOriginalFilename()));
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                IoUtil.copy(audioFile.getInputStream(), fos);
            }
            FFprobe ffprobe = new FFprobe();
            FFmpegProbeResult result = ffprobe.probe(tempFile.getAbsolutePath());
            duration = result.streams.get(0).duration;

        } finally {
            if (tempFile != null) {
                FileUtil.del(tempFile);
            }
        }
        String objectName = MinioUtils.uploadFile(audioFile, "temp", audioFile.getOriginalFilename());
        String tempUrl = MinioUtils.getPresignedUrl(objectName);
        // 防止url包含特殊字符导致的解析错误，将其base64编码
        String tempUrlBase64 = Base64.encode(tempUrl);
        Map<String, Object> map = Map.of("objectName", objectName, "duration", duration);
        RedisUtils.setJson(RedisKey.TEMP_AUDIO_URL, tempUrlBase64, map);
        log.info("上传音频成功 - objectName: {}, tempUrl: {}", objectName, tempUrl);
        return tempUrl;
    }

    @Override
    public String uploadCover(MultipartFile coverFile) throws Exception {
        String fileType = FileTypeUtil.getType(coverFile.getInputStream());
        if (fileType == null || !"jpg".equals(fileType) && !"jpeg".equals(fileType) && !"png".equals(fileType)) {
            throw new BaseException(400, "仅支持上传 JPG、JPEG、PNG 格式封面");
        }
        String objectName = MinioUtils.uploadFile(coverFile, "temp", coverFile.getOriginalFilename());
        String tempUrl = MinioUtils.getPresignedUrl(objectName);
        String tempUrlBase64 = Base64.encode(tempUrl);
        RedisUtils.set(RedisKey.TEMP_COVER_URL, tempUrlBase64, objectName);
        log.info("上传封面成功 - objectName: {}, tempUrl: {}", objectName, tempUrl);
        return tempUrl;
    }

    @Override
    public AudioPublishVO saveAudio(AudioDTO audioDTO) {
        log.info("保存音频 - audioDTO: {}", audioDTO);
        String audioUrlBase64 = Base64.encode(audioDTO.getAudioUrl());
        String coverUrlBase64 = Base64.encode(audioDTO.getCoverUrl());
        Map<String, Object> audioMap = RedisUtils.getJson(RedisKey.TEMP_AUDIO_URL, audioUrlBase64);
        String coverPath = RedisUtils.get(RedisKey.TEMP_COVER_URL, coverUrlBase64);

        if (audioMap == null || coverPath == null) {
            throw new BaseException(400, "无效文件，请重新上传");
        }
        AudioInfo audioInfo = new AudioInfo();
        BeanUtil.copyProperties(audioDTO, audioInfo);
        audioInfo.setCoverPath(coverPath);
        audioInfo.setRawPath((String) audioMap.get("objectName"));
        audioInfo.setDuration(((BigDecimal) audioMap.get("duration")).intValue());
        audioInfo.setCreatorId(SecurityUtils.getCurrentUserId());
        audioInfo.setStatus("PENDING_TRANSCODE");
        audioInfoMapper.insert(audioInfo);
        AudioPublishVO audioPublishVO = new AudioPublishVO();
        audioPublishVO.setAudioId(audioInfo.getId());
        audioPublishVO.setStatus("PENDING_TRANSCODE");
        RedisUtils.delete(RedisKey.TEMP_AUDIO_URL, audioUrlBase64);
        RedisUtils.delete(RedisKey.TEMP_COVER_URL, coverUrlBase64);

        // 发送转码任务到队列
        audioTranscodeProducer.sendTranscodeTask(audioInfo.getId(), (String) audioMap.get("objectName"),
                audioInfo.getTrialDuration());
        return audioPublishVO;
    }

    @Override
    public void MoveAudioToOnline(Long audioId) throws Exception {
        log.info("将音频移动到在线存储 - audioId: {}", audioId);
        AudioInfo audioInfo = audioInfoMapper.selectById(audioId);
        String pastRawPath = audioInfo.getRawPath();
        String pastCoverPath = audioInfo.getCoverPath();
        audioInfo.setRawPath(MinioUtils.copyFile(audioInfo.getRawPath(), "online/audio"));
        audioInfo.setCoverPath(MinioUtils.copyFile(audioInfo.getCoverPath(), "online/cover"));
        MinioUtils.removeFile(pastRawPath);
        MinioUtils.removeFile(pastCoverPath);
        audioInfoMapper.updateById(audioInfo);
    }

    @Override
    public IPage<CreatorAudioVO> getAudioPage(PageQuery pageQuery) {
        Page<AudioInfo> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        Long userId = SecurityUtils.getCurrentUserId();
        IPage<AudioInfo> audioInfoPage = audioInfoMapper.selectByCreatorId(userId, page);
        return audioInfoPage.convert(audio -> {
            CreatorAudioVO creatorAudioVO = new CreatorAudioVO();
            BeanUtil.copyProperties(audio, creatorAudioVO);
            creatorAudioVO.setCoverUrl(MinioUtils.getPresignedUrl(audio.getCoverPath()));
            return creatorAudioVO;
        });

    }

    @Override
    public CreatorAudioDetailVO getCreatorAudioDetail(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        AudioInfo audioInfo = audioInfoMapper.selectById(id);
        if (audioInfo == null || !audioInfo.getCreatorId().equals(userId)) {
            throw new BaseException(404, "稿件不存在");
        }
        CreatorAudioDetailVO creatorAudioDetailVO = new CreatorAudioDetailVO();
        BeanUtil.copyProperties(audioInfo, creatorAudioDetailVO);
        creatorAudioDetailVO.setCoverUrl(MinioUtils.getPresignedUrl(audioInfo.getCoverPath()));
        // TODO: 音频文字内容获取
        creatorAudioDetailVO.setPlayCount(audioInfo.getPlayCount());

        // TODO: 等添加 Like 统计字段后再启用
        // creatorAudioDetailVO.setLikeCount(audioInfo.getLikeCount());

        // TODO: 等添加 Collect 统计字段后再启用
        // creatorAudioDetailVO.setCollectCount(audioInfo.getCollectCount());

        return creatorAudioDetailVO;
    }

    @Override
    public AudioStatusVO getAudioStatus(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        AudioInfo audioInfo = audioInfoMapper.selectById(id);
        if (audioInfo == null || !audioInfo.getCreatorId().equals(userId)) {
            throw new BaseException(404, "稿件不存在");
        }
        AudioStatusVO audioStatusVO = new AudioStatusVO();
        audioStatusVO.setAudioId(id);
        audioStatusVO.setStatus(audioInfo.getStatus());
        if ("FAILED".equals(audioInfo.getStatus())) {
            audioStatusVO.setFailReason("转码失败");
        }
        return audioStatusVO;
    }

    @Override
    public void updateAudio(AudioUpdateDTO audioUpdateDTO) {
        AudioInfo audioInfo = audioInfoMapper.selectById(audioUpdateDTO.getId());
        if (audioInfo == null || !audioInfo.getCreatorId().equals(SecurityUtils.getCurrentUserId())) {
            throw new BaseException(404, "稿件不存在");
        }
        if (audioUpdateDTO.getCoverUrl() != null && !audioUpdateDTO.getCoverUrl().equals(audioInfo.getCoverPath())) {
            String coverUrlBase64 = Base64.encode(audioUpdateDTO.getCoverUrl());
            if (RedisUtils.get(RedisKey.TEMP_COVER_URL, coverUrlBase64) != null) {
                String coverPath = RedisUtils.get(RedisKey.TEMP_COVER_URL, coverUrlBase64);
                audioInfo.setCoverPath(coverPath);
            }
        }

        if (audioUpdateDTO.getTrialDuration() != null
                && !audioUpdateDTO.getTrialDuration().equals(audioInfo.getTrialDuration())) {
            audioInfo.setStatus("PENDING_TRANSCODE");
            audioTranscodeProducer.sendTranscodeTask(audioUpdateDTO.getId(), audioInfo.getRawPath(),
                    audioInfo.getTrialDuration());
        }
        audioInfo.setTitle(audioUpdateDTO.getTitle());
        audioInfo.setDescription(audioUpdateDTO.getDescription());
        audioInfo.setIsPaid(audioUpdateDTO.getIsPaid());
        audioInfo.setPrice(new BigDecimal(audioUpdateDTO.getPrice()));
        audioInfo.setVisibility(audioUpdateDTO.getVisibility());
        audioInfoMapper.updateById(audioInfo);
    }

    @Override
    public void removeAudioInfo(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        AudioInfo audioInfo = audioInfoMapper.selectById(id);
        if (audioInfo == null || !audioInfo.getCreatorId().equals(userId)) {
            throw new BaseException(404, "稿件不存在");
        }
        // 逻辑删除
        audioInfo.setIsDeleted((byte) 1);
        audioInfoMapper.updateById(audioInfo);
    }

    @Override
    public List<AudioVO> getHotList() {
        Set<Object> topIds = hotRankService.getTopN(10);
        if (topIds == null || topIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> audioIds = topIds.stream()
                .map(id -> Long.valueOf(id.toString()))
                .collect(Collectors.toList());

        List<AudioVO> result = audioVOMapper.selectByIds(audioIds);
        result.forEach(vo -> vo.setCoverUrl(MinioUtils.getPresignedUrl(vo.getCoverUrl())));

        // 保持热榜顺序
        Map<Long, AudioVO> idToVo = result.stream()
                .collect(Collectors.toMap(AudioVO::getId, Function.identity()));

        return audioIds.stream()
                .map(idToVo::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public IPage<AudioVO> searchAudio(AudioSearchQuery audioSearchQuery) {
        Page<AudioVO> page = new
                Page<>(audioSearchQuery.getPageNum(), audioSearchQuery.getPageSize());
        if ("TITLE".equals(audioSearchQuery.getSearchType())) {
            return audioVOMapper.selectByTitle(page, audioSearchQuery.getKeyword());
        }
        if ("CREATOR".equals(audioSearchQuery.getSearchType())) {
            return audioVOMapper.selectByCreator(page, audioSearchQuery.getKeyword());
        }
        if ("TRANSCRIPT".equals(audioSearchQuery.getSearchType())) {
            return audioVOMapper.selectByTranscript(page, audioSearchQuery.getKeyword());
        }
        throw new BaseException(400, "搜索类型无效，仅支持 TITLE、CREATOR、TRANSCRIPT");
    }
    @Transactional
    @Override
    public String getStreamSign(Long audioId) {
        if(audioId == null) {
            throw new BaseException(400, "音频ID不能为空");
        }

        Long userId = SecurityUtils.getCurrentUserId();

        AudioInfo audioInfo = audioInfoMapper.selectById(audioId);
        if(audioInfo == null ) {
            throw new BaseException(404, "音频不存在");
        }
        String suffix = userId + ":" + audioId;
        Object cache = RedisUtils.get(RedisKey.USER_PLAY_COUNTED, suffix);
        if (cache == null) {
            RedisUtils.set(RedisKey.USER_PLAY_COUNTED, suffix, "1");
            audioInfo.setPlayCount(audioInfo.getPlayCount() + 1);
            audioInfoMapper.updateById(audioInfo);
        }
        Wrapper<AudioOrder> wrapper = Wrappers.lambdaQuery(AudioOrder.class)
                .eq(AudioOrder::getAudioId, audioId)
                .eq(AudioOrder::getUserId, userId);

        AudioOrder audioOrder = audioOrderMapper.selectOne(wrapper);
        if(audioInfo.getIsPaid() == false || (audioOrder != null && audioOrder.getPayStatus() == 1)) {
            return MinioUtils.getPresignedUrl(audioInfo.getRawPath());
        }else if(audioInfo.getTrialDuration() != null && audioInfo.getStatus().equals("ONLINE")) {
            return MinioUtils.getPresignedUrl(audioInfo.getClipPath());
        }else {
            throw new BaseException(403, "请购买后收听完整版");
        }
    }

    @Override
    public AudioDetailVO getAudioDetail(Long id) {
        AudioDetailVO audioDetailVO = new AudioDetailVO();
        AudioInfo audioInfo = audioInfoMapper.selectById(id);
        BeanUtil.copyProperties(audioInfo, audioDetailVO);
        audioDetailVO.setCoverUrl(MinioUtils.getPresignedUrl(audioInfo.getCoverPath()));

        Wrapper<PlayHistory> playHistoryWrapper = Wrappers.lambdaQuery(PlayHistory.class)
                .eq(PlayHistory::getAudioId, id)
                .eq(PlayHistory::getUserId, SecurityUtils.getCurrentUserId());

        PlayHistory playHistory = playHistoryMapper.selectOne(playHistoryWrapper);

        if(playHistory != null) {
            audioDetailVO.setProgress(playHistory.getLastPosition());
        }else {
            audioDetailVO.setProgress(0);
        }

        Wrapper<AudioOrder> wrapper = Wrappers.lambdaQuery(AudioOrder.class)
                .eq(AudioOrder::getAudioId, id)
                .eq(AudioOrder::getUserId, SecurityUtils.getCurrentUserId());

        AudioOrder audioOrder = audioOrderMapper.selectOne(wrapper);
        if(audioInfo.getIsPaid() == false && (audioOrder == null || audioOrder.getPayStatus() != 1)) {
            audioDetailVO.setIsPurchased(false);
        }else {
            audioDetailVO.setIsPurchased(true);
        }
        AudioDetailVO.CreatorInfo creatorInfo = new AudioDetailVO.CreatorInfo();
        creatorInfo.setId(audioInfo.getCreatorId());
        SysUser user = sysUserMapper.selectById(audioInfo.getCreatorId());
        creatorInfo.setNickname(user.getNickname());
        creatorInfo.setAvatar(user.getAvatar());
        audioDetailVO.setCreator(creatorInfo);

        AudioDetailVO.StatsInfo statsInfo = new AudioDetailVO.StatsInfo();
        statsInfo.setPlayCount(Long.valueOf(audioInfo.getPlayCount()));
        statsInfo.setLikeCount(Long.valueOf(audioLikeMapper.selectCount(Wrappers.lambdaQuery(AudioLike.class)
                .eq(AudioLike::getAudioId, id))));
        statsInfo.setCollectCount(Long.valueOf(audioFolderRelationMapper.selectCount(Wrappers.lambdaQuery(AudioFolderRelation.class)
                .eq(AudioFolderRelation::getAudioId, id))));
        audioDetailVO.setStats(statsInfo);
        return audioDetailVO;
    }

    @Override
    public IPage<AuditAudioVO> getAuditAudioPage(AuditQuery auditQuery) {
        if(!"ALL".equals(auditQuery.getStatus()) && !"PENDING".equals(auditQuery.getStatus())
                && !"APPROVED".equals(auditQuery.getStatus()) && !"REJECTED".equals(auditQuery.getStatus())){
            throw new BaseException(400, "审核状态无效，仅支持 PENDING、ALL、APPROVED、REJECTED");
        }
        Page<AuditAudioVO> page = new Page<>(auditQuery.getPageNum(), auditQuery.getPageSize());
        IPage<AuditAudioVO> pageResult = audioInfoMapper.selectAuditAudioPage(page, auditQuery.getStatus());
        pageResult.getRecords().forEach(vo -> vo.setCoverUrl(MinioUtils.getPresignedUrl(vo.getCoverUrl())));
        return pageResult;
    }

    @Override
    public void auditAudio(AudioAuditDTO audioAuditDTO) {
        AudioInfo audioInfo = this.getById(audioAuditDTO.getAudioId());
        if(audioInfo == null
                || audioInfo.getAuditStatus() != 0
                || audioInfo.getIsDeleted() == 1) {
            throw new BaseException(400, "音频不存在或已处理");
        }
        if("APPROVED".equals(audioAuditDTO.getStatus())) {
            audioInfo.setAuditStatus(1);
            // TODO 触发上线通知、推荐索引更新等
        }else if("REJECTED".equals(audioAuditDTO.getStatus())) {
            // TODO 通知创作者驳回原因
            audioInfo.setAuditStatus(2);
            audioInfo.setRejectReason(audioAuditDTO.getRejectReason());
        }else {
            throw new BaseException(400, "审核状态无效，仅支持 APPROVED、REJECTED");
        }
        audioInfoMapper.updateById(audioInfo);
    }
}
