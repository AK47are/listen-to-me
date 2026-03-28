package com.github.listen_to_me.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.listen_to_me.domain.dto.FavoriteActionDTO;
import com.github.listen_to_me.domain.dto.FavoriteDeleteDTO;
import com.github.listen_to_me.domain.entity.AudioFolderRelation;

public interface AudioFolderRelationService extends IService<AudioFolderRelation> {
    public void saveAudioAction(FavoriteActionDTO favoriteActionDTO);

    void deleteFavorite(FavoriteDeleteDTO favoriteDeleteDTO);
}
