package com.github.listen_to_me.service.impl;

import com.github.listen_to_me.domain.entity.AudioInfo;
import com.github.listen_to_me.mapper.AudioInfoMapper;
import com.github.listen_to_me.service.IAudioInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author kun
 * @since 2026-03-24
 */
@Service
public class AudioInfoServiceImpl extends ServiceImpl<AudioInfoMapper, AudioInfo> implements IAudioInfoService {

}
