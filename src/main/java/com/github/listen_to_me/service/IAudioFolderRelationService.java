package com.github.listen_to_me.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.listen_to_me.domain.entity.AudioFolderRelation;
import com.github.listen_to_me.domain.vo.FolderVO;

public interface IAudioFolderRelationService extends IService<AudioFolderRelation> {
    void collectAudio(Long userId, Long audioId, Long folderId);

    void uncollectAudio(Long userId, Long audioId, Long folderId);

    List<FolderVO> getAudioFolders(Long userId, Long audioId);
}
