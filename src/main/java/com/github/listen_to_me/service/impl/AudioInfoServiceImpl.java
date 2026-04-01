package com.github.listen_to_me.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.file.FileNameUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.listen_to_me.common.enumeration.RedisKey;
import com.github.listen_to_me.common.exception.BaseException;
import com.github.listen_to_me.common.producer.AudioTranscodeProducer;
import com.github.listen_to_me.common.util.MinioUtils;
import com.github.listen_to_me.common.util.RedisUtils;
import com.github.listen_to_me.common.util.SecurityUtils;
import com.github.listen_to_me.domain.dto.AudioDTO;
import com.github.listen_to_me.domain.dto.AudioUpdateDTO;
import com.github.listen_to_me.domain.dto.CreatorAudioDetailVO;
import com.github.listen_to_me.domain.entity.AudioFolderRelation;
import com.github.listen_to_me.domain.entity.AudioInfo;
import com.github.listen_to_me.domain.entity.AudioLike;
import com.github.listen_to_me.domain.entity.PlayHistory;
import com.github.listen_to_me.domain.query.FavoriteQuery;
import com.github.listen_to_me.domain.query.PageQuery;
import com.github.listen_to_me.domain.vo.AudioPublishVO;
import com.github.listen_to_me.domain.vo.AudioStatusVO;
import com.github.listen_to_me.domain.vo.AudioVO;
import com.github.listen_to_me.domain.vo.CreatorAudioVO;
import com.github.listen_to_me.mapper.AudioFolderRelationMapper;
import com.github.listen_to_me.mapper.AudioInfoMapper;
import com.github.listen_to_me.mapper.AudioLikeMapper;
import com.github.listen_to_me.mapper.PlayHistoryMapper;
import com.github.listen_to_me.service.IAudioInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author kun
 * @since 2026-03-24
 */
@Service
@AllArgsConstructor
@Slf4j
public class AudioInfoServiceImpl extends ServiceImpl<AudioInfoMapper, AudioInfo> implements IAudioInfoService {
    private final AudioInfoMapper audioInfoMapper;
    // 注入生产者
    @Resource
    private AudioTranscodeProducer audioTranscodeProducer;

    private final PlayHistoryMapper playHistoryMapper;

    private final AudioLikeMapper audioLikeMapper;

    private final AudioFolderRelationMapper audioFolderRelationMapper;
    @Override
    public IPage<AudioVO> getFavoriteAudioPage(FavoriteQuery favoriteQuery) {
        // 构建分页
        Page<AudioInfo> page = new Page<>(favoriteQuery.getPageNum(), favoriteQuery.getPageSize());

        // 直接调用 mapper 联表分页查询
        IPage<AudioInfo> audioPage = audioInfoMapper.selectAudioByFolderId(page, favoriteQuery.getFolderId());
        return audioPage.convert(audio -> BeanUtil.copyProperties(audio, AudioVO.class));
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


        //发送转码任务到队列
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
    public CreatorAudioDetailVO getAudioDetail(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        AudioInfo audioInfo = audioInfoMapper.selectById(id);
        if(audioInfo == null || !audioInfo.getCreatorId().equals(userId)){
            throw new BaseException(404, "稿件不存在");
        }
        CreatorAudioDetailVO creatorAudioDetailVO = new CreatorAudioDetailVO();
        BeanUtil.copyProperties(audioInfo, creatorAudioDetailVO);
        creatorAudioDetailVO.setCoverUrl(MinioUtils.getPresignedUrl(audioInfo.getCoverPath()));
        //TODO: 音频文字内容获取
        creatorAudioDetailVO.setPlayCount(audioInfo.getPlayCount());

        creatorAudioDetailVO.setLikeCount(audioInfo.getLikeCount());

        creatorAudioDetailVO.setCollectCount(audioInfo.getCollectCount());

        return creatorAudioDetailVO;
    }

    @Override
    public AudioStatusVO getAudioStatus(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        AudioInfo audioInfo = audioInfoMapper.selectById(id);
        if(audioInfo == null || !audioInfo.getCreatorId().equals(userId)){
            throw new BaseException(404, "稿件不存在");
        }
        AudioStatusVO audioStatusVO = new AudioStatusVO();
        audioStatusVO.setAudioId(id);
        audioStatusVO.setStatus(audioInfo.getStatus());
        if("FAILED".equals(audioInfo.getStatus())){
            audioStatusVO.setFailReason("转码失败");
        }
        return audioStatusVO;
    }

    @Override
    public void updateAudio(AudioUpdateDTO audioUpdateDTO) {
        AudioInfo audioInfo = audioInfoMapper.selectById(audioUpdateDTO.getId());
        if(audioInfo == null || !audioInfo.getCreatorId().equals(SecurityUtils.getCurrentUserId())) {
            throw new BaseException(404, "稿件不存在");
        }
        if(audioUpdateDTO.getCoverUrl() != null && !audioUpdateDTO.getCoverUrl().equals(audioInfo.getCoverPath())){
            String coverUrlBase64 = Base64.encode(audioUpdateDTO.getCoverUrl());
            if(RedisUtils.get(RedisKey.TEMP_COVER_URL, coverUrlBase64) != null) {
                String coverPath =  RedisUtils.get(RedisKey.TEMP_COVER_URL, coverUrlBase64);
                audioInfo.setCoverPath(coverPath);
            }
        }

        if(audioUpdateDTO.getTrialDuration() != null && !audioUpdateDTO.getTrialDuration().equals(audioInfo.getTrialDuration())){
            audioInfo.setStatus("PENDING_TRANSCODE");
            audioTranscodeProducer.sendTranscodeTask(audioUpdateDTO.getId(), audioInfo.getRawPath(), audioInfo.getTrialDuration());
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
        if(audioInfo == null || !audioInfo.getCreatorId().equals(userId)){
            throw new BaseException(404, "稿件不存在");
        }
        //逻辑删除
        audioInfo.setIsDeleted((byte) 1);
        audioInfoMapper.updateById(audioInfo);
    }
}
