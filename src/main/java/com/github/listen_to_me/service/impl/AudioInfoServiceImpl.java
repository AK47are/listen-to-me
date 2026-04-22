package com.github.listen_to_me.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.listen_to_me.common.enumeration.RedisKey;
import com.github.listen_to_me.common.exception.BaseException;
import com.github.listen_to_me.common.producer.AudioTranscodeProducer;
import com.github.listen_to_me.common.util.MinioUtils;
import com.github.listen_to_me.common.util.RedisUtils;
import com.github.listen_to_me.domain.dto.AudioAuditDTO;
import com.github.listen_to_me.domain.dto.AudioDTO;
import com.github.listen_to_me.domain.dto.AudioUpdateDTO;
import com.github.listen_to_me.domain.entity.AudioFolderRelation;
import com.github.listen_to_me.domain.entity.AudioInfo;
import com.github.listen_to_me.domain.entity.AudioLike;
import com.github.listen_to_me.domain.entity.AudioOrder;
import com.github.listen_to_me.domain.entity.AudioSummary;
import com.github.listen_to_me.domain.entity.AudioTranscript;
import com.github.listen_to_me.domain.entity.SysUser;
import com.github.listen_to_me.domain.entity.UserFollow;
import com.github.listen_to_me.domain.query.AudioSearchQuery;
import com.github.listen_to_me.domain.query.AuditQuery;
import com.github.listen_to_me.domain.query.FavoriteQuery;
import com.github.listen_to_me.domain.query.PageQuery;
import com.github.listen_to_me.domain.vo.AudioDetailVO;
import com.github.listen_to_me.domain.vo.AudioPublishVO;
import com.github.listen_to_me.domain.vo.AudioStatusVO;
import com.github.listen_to_me.domain.vo.AudioVO;
import com.github.listen_to_me.domain.vo.AuditAudioVO;
import com.github.listen_to_me.domain.vo.CreatorAudioDetailVO;
import com.github.listen_to_me.domain.vo.CreatorAudioVO;
import com.github.listen_to_me.mapper.AudioFolderRelationMapper;
import com.github.listen_to_me.mapper.AudioInfoMapper;
import com.github.listen_to_me.mapper.AudioLikeMapper;
import com.github.listen_to_me.mapper.AudioOrderMapper;
import com.github.listen_to_me.mapper.AudioSummaryMapper;
import com.github.listen_to_me.mapper.AudioTranscriptMapper;
import com.github.listen_to_me.mapper.AudioVOMapper;
import com.github.listen_to_me.mapper.SysUserMapper;
import com.github.listen_to_me.mapper.UserFollowMapper;
import com.github.listen_to_me.service.HotRankService;
import com.github.listen_to_me.service.IAudioInfoService;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FileTypeUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    private final SysUserMapper sysUserMapper;
    private final AudioLikeMapper audioLikeMapper;
    private final AudioFolderRelationMapper audioFolderRelationMapper;
    private final AudioTranscriptMapper audioTranscriptMapper;
    private final UserFollowMapper userFollowMapper;
    private final AudioSummaryMapper audioSummaryMapper;

    @Override
    public IPage<AudioVO> getFavoriteAudioPage(FavoriteQuery favoriteQuery) {
        Page<AudioVO> page = new Page<>(favoriteQuery.getPageNum(), favoriteQuery.getPageSize());
        IPage<AudioVO> result = audioVOMapper.selectByFolderId(page, favoriteQuery.getFolderId());

        result.getRecords().forEach(vo -> vo.setCoverUrl(MinioUtils.getPresignedUrl(vo.getCoverUrl())));

        return result;
    }

    @Override
    public String uploadAudio(MultipartFile audioFile) throws Exception {
        String fileType = FileTypeUtil.getType(audioFile.getInputStream());
        if (fileType == null || !"mp3".equals(fileType)) {
            throw new BaseException(400, "仅支持上传 MP3 格式音频");
        }
        String objectName = MinioUtils.uploadFile(audioFile, "temp", audioFile.getOriginalFilename());
        String tempUrl = MinioUtils.getPresignedUrl(objectName);
        // 防止url包含特殊字符导致的解析错误，将其base64编码
        String tempUrlBase64 = Base64.encode(tempUrl);
        RedisUtils.set(RedisKey.TEMP_AUDIO_URL, tempUrlBase64, objectName);
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
    public AudioPublishVO saveAudio(Long userId, AudioDTO audioDTO) {
        log.info("保存音频 - userId: {}, audioDTO: {}", userId, audioDTO);
        String audioUrlBase64 = Base64.encode(audioDTO.getAudioUrl());
        String coverUrlBase64 = Base64.encode(audioDTO.getCoverUrl());
        String objectName = RedisUtils.get(RedisKey.TEMP_AUDIO_URL, audioUrlBase64);
        String coverPath = RedisUtils.get(RedisKey.TEMP_COVER_URL, coverUrlBase64);

        if (objectName == null || coverPath == null) {
            throw new BaseException(400, "无效文件，请重新上传");
        }
        AudioInfo audioInfo = new AudioInfo();
        BeanUtil.copyProperties(audioDTO, audioInfo);
        audioInfo.setCoverPath(coverPath);
        audioInfo.setRawPath(objectName);
        audioInfo.setCreatorId(userId);
        audioInfo.setStatus("PENDING_TRANSCODE");
        audioInfoMapper.insert(audioInfo);
        AudioPublishVO audioPublishVO = new AudioPublishVO();
        audioPublishVO.setAudioId(audioInfo.getId());
        audioPublishVO.setStatus("PENDING_TRANSCODE");
        RedisUtils.delete(RedisKey.TEMP_AUDIO_URL, audioUrlBase64);
        RedisUtils.delete(RedisKey.TEMP_COVER_URL, coverUrlBase64);

        // 发送转码任务到队列
        audioTranscodeProducer.sendTranscodeTask(audioInfo.getId(), objectName,
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
    public IPage<CreatorAudioVO> getAudioPage(Long userId, PageQuery pageQuery) {
        Page<CreatorAudioVO> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        IPage<CreatorAudioVO> result = audioInfoMapper.selectCreatorAudioPage(page, userId);
        result.getRecords().forEach(vo -> vo.setCoverUrl(MinioUtils.getPresignedUrl(vo.getCoverUrl())));
        return result;
    }

    @Override
    public CreatorAudioDetailVO getCreatorAudioDetail(Long creatorId, Long audioId) {
        AudioInfo audioInfo = audioInfoMapper.selectById(audioId);
        if (audioInfo == null || !audioInfo.getCreatorId().equals(creatorId)) {
            throw new BaseException(404, "稿件不存在");
        }

        CreatorAudioDetailVO vo = new CreatorAudioDetailVO();
        vo.setId(audioInfo.getId());
        vo.setTitle(audioInfo.getTitle());
        vo.setCoverUrl(MinioUtils.getPresignedUrl(audioInfo.getCoverPath()));
        vo.setDescription(audioInfo.getDescription());
        vo.setTrialDuration(audioInfo.getTrialDuration());
        vo.setIsPaid(audioInfo.getIsPaid());
        vo.setPrice(audioInfo.getPrice());
        vo.setVisibility(audioInfo.getVisibility());

        return vo;
    }

    @Override
    public AudioStatusVO getAudioStatus(Long userId, Long id) {
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
    public void updateAudio(Long userId, AudioUpdateDTO audioUpdateDTO) {
        AudioInfo audioInfo = audioInfoMapper.selectById(audioUpdateDTO.getId());
        if (audioInfo == null || !audioInfo.getCreatorId().equals(userId)) {
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
    public void removeAudioInfo(Long creatorId, Long audioId) {
        AudioInfo audioInfo = audioInfoMapper.selectById(audioId);
        if (audioInfo == null || !audioInfo.getCreatorId().equals(creatorId)) {
            throw new BaseException(404, "稿件不存在");
        }
        // 逻辑删除, 配置了 @TableLogic 会自动进行逻辑删除
        audioInfoMapper.deleteById(audioId);
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
        Page<AudioVO> page = new Page<>(audioSearchQuery.getPageNum(), audioSearchQuery.getPageSize());
        IPage<AudioVO> result;
        if ("TITLE".equals(audioSearchQuery.getSearchType())) {
            result = audioVOMapper.selectByTitle(page, audioSearchQuery.getKeyword());
        } else if ("CREATOR".equals(audioSearchQuery.getSearchType())) {
            result = audioVOMapper.selectByCreator(page, audioSearchQuery.getKeyword());
        } else if ("TRANSCRIPT".equals(audioSearchQuery.getSearchType())) {
            result = audioVOMapper.selectByTranscript(page, audioSearchQuery.getKeyword());
        } else {
            throw new BaseException(400, "搜索类型无效，仅支持 TITLE、CREATOR、TRANSCRIPT");
        }

        result.getRecords().forEach(vo -> vo.setCoverUrl(MinioUtils.getPresignedUrl(vo.getCoverUrl())));
        return result;
    }

    @Transactional
    @Override
    public String getStreamSign(Long userId, Long audioId) {
        if (audioId == null) {
            throw new BaseException(400, "音频ID不能为空");
        }

        AudioInfo audioInfo = audioInfoMapper.selectById(audioId);
        if (audioInfo == null) {
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
        if (audioInfo.getIsPaid() == false || (audioOrder != null && audioOrder.getPayStatus() == 1)) {
            return MinioUtils.getPresignedUrl(audioInfo.getRawPath());
        } else if (audioInfo.getTrialDuration() != null && audioInfo.getStatus().equals("ONLINE")) {
            return MinioUtils.getPresignedUrl(audioInfo.getClipPath());
        } else {
            throw new BaseException(403, "请购买后收听完整版");
        }
    }

    @Override
    public AudioDetailVO getAudioDetail(Long userId, Long audioId) {
        AudioInfo audioInfo = audioInfoMapper.selectById(audioId);
        if (audioInfo == null) {
            throw new BaseException(404, "音频不存在");
        }

        AudioDetailVO audioDetailVO = new AudioDetailVO();
        BeanUtil.copyProperties(audioInfo, audioDetailVO);
        audioDetailVO.setCoverUrl(MinioUtils.getPresignedUrl(audioInfo.getCoverPath()));

        // 是否已购买
        if (!audioInfo.getIsPaid()) {
            audioDetailVO.setIsPurchased(true);
        } else {
            Wrapper<AudioOrder> orderWrapper = Wrappers.lambdaQuery(AudioOrder.class)
                    .eq(AudioOrder::getAudioId, audioId)
                    .eq(AudioOrder::getUserId, userId)
                    .eq(AudioOrder::getPayStatus, 1);
            audioDetailVO.setIsPurchased(audioOrderMapper.selectCount(orderWrapper) > 0);
        }

        // 是否已喜欢
        Wrapper<AudioLike> likeWrapper = Wrappers.lambdaQuery(AudioLike.class)
                .eq(AudioLike::getAudioId, audioId)
                .eq(AudioLike::getUserId, userId);
        audioDetailVO.setIsLike(audioLikeMapper.selectCount(likeWrapper) > 0);

        // 创作者信息
        AudioDetailVO.CreatorInfo creatorInfo = new AudioDetailVO.CreatorInfo();
        creatorInfo.setId(audioInfo.getCreatorId());
        SysUser user = sysUserMapper.selectById(audioInfo.getCreatorId());
        if (user != null) {
            creatorInfo.setNickname(user.getNickname());
            creatorInfo.setAvatar(MinioUtils.getPresignedUrl(user.getAvatar()));
        }

        // 是否关注创作者
        Wrapper<UserFollow> followWrapper = Wrappers.lambdaQuery(UserFollow.class)
                .eq(UserFollow::getCreatorId, audioInfo.getCreatorId())
                .eq(UserFollow::getUserId, userId);
        creatorInfo.setIsFollow(userFollowMapper.selectCount(followWrapper) > 0);
        audioDetailVO.setCreator(creatorInfo);

        // 统计数据
        AudioDetailVO.StatsInfo statsInfo = new AudioDetailVO.StatsInfo();
        statsInfo.setPlayCount(Long.valueOf(audioInfo.getPlayCount()));
        statsInfo.setLikeCount(Long.valueOf(audioLikeMapper.selectCount(
                Wrappers.lambdaQuery(AudioLike.class).eq(AudioLike::getAudioId, audioId))));
        statsInfo.setCollectCount(Long.valueOf(audioFolderRelationMapper.selectCount(
                Wrappers.lambdaQuery(AudioFolderRelation.class).eq(AudioFolderRelation::getAudioId, audioId))));
        audioDetailVO.setStats(statsInfo);

        // 转写和摘要（仅免费音频或已购买用户可见）
        if (!audioInfo.getIsPaid() || audioDetailVO.getIsPurchased()) {
            AudioTranscript transcript = audioTranscriptMapper.selectOne(
                    Wrappers.lambdaQuery(AudioTranscript.class).eq(AudioTranscript::getAudioId, audioId));
            if (transcript != null) {
                audioDetailVO.setTranscript(transcript.getFullText());
            }

            AudioSummary summary = audioSummaryMapper.selectOne(
                    Wrappers.lambdaQuery(AudioSummary.class).eq(AudioSummary::getAudioId, audioId));
            if (summary != null) {
                audioDetailVO.setSummary(summary.getSummary());
            }
        }

        return audioDetailVO;
    }

    @Override
    public IPage<AuditAudioVO> getAuditAudioPage(AuditQuery auditQuery) {
        if (!"ALL".equals(auditQuery.getStatus()) && !"PENDING".equals(auditQuery.getStatus())
                && !"APPROVED".equals(auditQuery.getStatus()) && !"REJECTED".equals(auditQuery.getStatus())) {
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
        if (audioInfo == null
                || !"PENDING".equals(audioInfo.getAuditStatus())
                || audioInfo.getIsDeleted() == 1) {
            throw new BaseException(400, "音频不存在或已处理");
        }
        if ("APPROVED".equals(audioAuditDTO.getStatus())) {
            audioInfo.setAuditStatus("APPROVED");
            // TODO 触发上线通知、推荐索引更新等
        } else if ("REJECTED".equals(audioAuditDTO.getStatus())) {
            // TODO 通知创作者驳回原因
            audioInfo.setAuditStatus("REJECTED");
            audioInfo.setRejectReason(audioAuditDTO.getRejectReason());
        } else {
            throw new BaseException(400, "审核状态无效，仅支持 APPROVED、REJECTED");
        }
        audioInfoMapper.updateById(audioInfo);
    }

    @Override
    public IPage<AudioVO> getCreatorAudioPage(Long creatorId, PageQuery pageQuery) {
        log.debug("分页查询创作者作品 - 创作者ID: {}", creatorId);
        Page<AudioVO> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        IPage<AudioVO> result = audioVOMapper.selectByCreatorId(page, creatorId);

        result.getRecords().forEach(vo -> vo.setCoverUrl(MinioUtils.getPresignedUrl(vo.getCoverUrl())));

        return result;
    }

    @Override
    public IPage<AudioVO> getRecommendList(Long userId, PageQuery pageQuery) {
        List<AudioVO> recommendList = audioVOMapper.selectLikedRecommendByUserId(userId);
        if (recommendList == null) {
            recommendList = new ArrayList<>();
        }
        recommendList.addAll(this.getHotList());

        Map<Long, AudioVO> map = new LinkedHashMap<>();
        for (AudioVO vo : recommendList) {
            map.putIfAbsent(vo.getId(), vo);
        }
        List<AudioVO> distinctList = new ArrayList<>(map.values());
        Collections.shuffle(distinctList);

        Integer current = pageQuery.getPageNum();
        Integer size = pageQuery.getPageSize();
        int start = (current - 1) * size;
        int end = Math.min(start + size, distinctList.size());
        Page<AudioVO> page = new Page<>(current, size);
        if (start < end) {
            page.setRecords(distinctList.subList(start, end));
        } else {
            page.setRecords(Collections.emptyList());
        }
        page.setTotal(distinctList.size());

        return page;
    }
}
