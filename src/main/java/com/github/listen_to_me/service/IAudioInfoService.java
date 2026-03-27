package com.github.listen_to_me.service;

import com.github.listen_to_me.domain.dto.AudioDTO;
import com.github.listen_to_me.domain.entity.AudioInfo;
import com.baomidou.mybatisplus.extension.service.IService;
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

    void addAudio(AudioDTO audioDTO);

    String uploadAudio(MultipartFile file) throws Exception;
}
