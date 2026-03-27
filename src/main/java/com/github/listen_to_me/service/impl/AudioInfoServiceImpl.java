package com.github.listen_to_me.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.github.listen_to_me.common.util.MinioUtils;
import com.github.listen_to_me.domain.dto.AudioDTO;
import com.github.listen_to_me.domain.entity.AudioInfo;
import com.github.listen_to_me.domain.entity.AudioTagRelation;
import com.github.listen_to_me.mapper.AudioInfoMapper;
import com.github.listen_to_me.mapper.AudioTagRelationMapper;
import com.github.listen_to_me.service.IAudioInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author kun
 * @since 2026-03-24
 */
@Slf4j
@AllArgsConstructor
@Service
public class AudioInfoServiceImpl extends ServiceImpl<AudioInfoMapper, AudioInfo> implements IAudioInfoService {
    private  final AudioInfoMapper audioInfoMapper;
    private final AudioTagRelationMapper audioTagRelationMapper;
    @Override
    @Transactional
    public void addAudio(AudioDTO audioDTO) {
        AudioInfo audioInfo = BeanUtil.copyProperties(audioDTO, AudioInfo.class);
        log.info("插入 audioInfo: {}", audioInfo);
        audioInfoMapper.insert(audioInfo);
        Long audioId = audioInfo.getId();
        log.info("插入 audioInfo 成功，id: {}", audioId);
        List<Long> tagIds = audioDTO.getTagIds();
        log.info("插入 audioTagRelation: {}", tagIds);
        if (tagIds != null && !tagIds.isEmpty()) {
            audioTagRelationMapper.insertBatch(audioId, tagIds);
        }
    }

}
