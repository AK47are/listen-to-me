package com.github.listen_to_me.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.listen_to_me.domain.entity.AudioTagRelation;
import com.github.listen_to_me.mapper.AudioTagRelationMapper;
import com.github.listen_to_me.service.IAudioTagRelationService;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author kun
 * @since 2026-03-24
 */
@Service
public class AudioTagRelationServiceImpl extends ServiceImpl<AudioTagRelationMapper, AudioTagRelation>
        implements IAudioTagRelationService {

}
