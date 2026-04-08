package com.github.listen_to_me.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.listen_to_me.common.exception.BaseException;
import com.github.listen_to_me.common.exception.ConflictException;
import com.github.listen_to_me.common.util.SecurityUtils;
import com.github.listen_to_me.domain.dto.FavoriteActionDTO;
import com.github.listen_to_me.domain.dto.FavoriteDeleteDTO;
import com.github.listen_to_me.domain.entity.AudioFolderRelation;
import com.github.listen_to_me.domain.vo.FolderVO;
import com.github.listen_to_me.mapper.AudioFolderRelationMapper;
import com.github.listen_to_me.service.IAudioFolderRelationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AudioFolderRelationServiceImpl extends ServiceImpl<AudioFolderRelationMapper, AudioFolderRelation>
        implements IAudioFolderRelationService {
    private final AudioFolderRelationMapper audioFolderRelationMapper;

    @Override
    public void saveAudioAction(FavoriteActionDTO favoriteActionDTO) {
        Wrapper<AudioFolderRelation> wrapper = Wrappers.lambdaQuery(AudioFolderRelation.class)
                .eq(AudioFolderRelation::getAudioId, favoriteActionDTO.getAudioId())
                .eq(AudioFolderRelation::getFolderId, favoriteActionDTO.getFolderId());
        if (favoriteActionDTO.getAction().equals("COLLECT")) {
            if (audioFolderRelationMapper.selectOne(wrapper) != null) {
                throw new ConflictException("音频已存在收藏");
            }
            AudioFolderRelation audioFolderRelation = new AudioFolderRelation();
            audioFolderRelation.setAudioId(favoriteActionDTO.getAudioId());
            audioFolderRelation.setFolderId(favoriteActionDTO.getFolderId());
            audioFolderRelationMapper.insert(audioFolderRelation);

        } else if (favoriteActionDTO.getAction().equals("UNCOLLECT")) {
            if (audioFolderRelationMapper.selectOne(wrapper) == null) {
                throw new BaseException(404, "音频不存在收藏");
            }
            audioFolderRelationMapper.delete(wrapper);

        } else {
            throw new BaseException("前端操作错误");
        }
    }

    @Override
    public void deleteFavorite(FavoriteDeleteDTO favoriteDeleteDTO) {
        Wrapper<AudioFolderRelation> wrapper = Wrappers.lambdaQuery(AudioFolderRelation.class)
                .eq(AudioFolderRelation::getAudioId, favoriteDeleteDTO.getAudioId())
                .eq(AudioFolderRelation::getFolderId, favoriteDeleteDTO.getFolderId());
        if (audioFolderRelationMapper.selectOne(wrapper) == null) {
            throw new BaseException(404, "音频不存在收藏");
        }
        audioFolderRelationMapper.delete(wrapper);
    }

    @Override
    public List<FolderVO> getAudioFolders(Long audioId) {
        Long currId = SecurityUtils.getCurrentUserId();
        return audioFolderRelationMapper.selectAudioFolders(currId, audioId);
    }
}
