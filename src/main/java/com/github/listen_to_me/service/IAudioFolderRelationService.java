package com.github.listen_to_me.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.listen_to_me.domain.dto.FavoriteActionDTO;
import com.github.listen_to_me.domain.entity.AudioFolderRelation;
import com.github.listen_to_me.domain.vo.FolderVO;

public interface IAudioFolderRelationService extends IService<AudioFolderRelation> {
    public void saveAudioAction(FavoriteActionDTO favoriteActionDTO);

    List<FolderVO> getAudioFolders(Long audioId);
}
