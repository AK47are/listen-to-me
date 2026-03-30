package com.github.listen_to_me.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.file.FileNameUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.listen_to_me.common.enumeration.RedisKey;
import com.github.listen_to_me.common.exception.BaseException;
import com.github.listen_to_me.common.util.MinioUtils;
import com.github.listen_to_me.common.util.RedisUtils;
import com.github.listen_to_me.domain.entity.AudioInfo;
import com.github.listen_to_me.domain.query.FavoriteQuery;
import com.github.listen_to_me.domain.vo.AudioVO;
import com.github.listen_to_me.mapper.AudioInfoMapper;
import com.github.listen_to_me.service.IAudioInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
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
public class AudioInfoServiceImpl extends ServiceImpl<AudioInfoMapper, AudioInfo> implements IAudioInfoService {
    private final AudioInfoMapper audioInfoMapper;

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

        String fileType = FileTypeUtil.getType(audioFile.getInputStream());
        if (fileType == null || !"mp3".equals(fileType)) {
            throw new BaseException(400, "仅支持上传 MP3 格式音频");
        }
        File tempFile = null;
        double duration = 0;
        try {
            tempFile = File.createTempFile("temp_audio_", "." + FileNameUtil.getSuffix(audioFile.getOriginalFilename()));
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
        String objectName = MinioUtils.uploadFile(audioFile,"temp",audioFile.getOriginalFilename());
        String tempUrl = MinioUtils.getPresignedUrl(objectName);
        //防止url包含特殊字符导致的解析错误，将其base64编码
        String tempUrlBase64 = Base64.encode(tempUrl);
        Map<String, Object> map = Map.of("objectName", objectName, "duration", duration);
        RedisUtils.setJson(RedisKey.TEMP_AUDIO_URL, tempUrlBase64, map);
        return tempUrl;
    }
}
