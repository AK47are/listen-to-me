package com.github.listen_to_me.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.listen_to_me.domain.dto.FavoriteActionDTO;
import com.github.listen_to_me.domain.dto.FavoriteDeleteDTO;
import com.github.listen_to_me.domain.entity.AudioFolderRelation;
import com.github.listen_to_me.domain.vo.FolderVO;

import java.util.List;

public interface IAudioFolderRelationService extends IService<AudioFolderRelation> {
    public void saveAudioAction(FavoriteActionDTO favoriteActionDTO);

    void deleteFavorite(FavoriteDeleteDTO favoriteDeleteDTO);

    List<FolderVO> getAudioFolders(Long audioId);
}
