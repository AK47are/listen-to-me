package com.github.listen_to_me.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.listen_to_me.common.exception.BaseException;
import com.github.listen_to_me.common.exception.ConflictException;
import com.github.listen_to_me.domain.entity.AudioFolderRelation;
import com.github.listen_to_me.domain.entity.Folder;
import com.github.listen_to_me.domain.vo.FolderVO;
import com.github.listen_to_me.mapper.AudioFolderRelationMapper;
import com.github.listen_to_me.mapper.AudioInfoMapper;
import com.github.listen_to_me.mapper.FolderMapper;
import com.github.listen_to_me.service.IAudioFolderRelationService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AudioFolderRelationServiceImpl extends ServiceImpl<AudioFolderRelationMapper, AudioFolderRelation>
        implements IAudioFolderRelationService {

    private final AudioFolderRelationMapper audioFolderRelationMapper;
    private final FolderMapper folderMapper;
    private final AudioInfoMapper audioInfoMapper;

    @Override
    @Transactional
    public void collectAudio(Long userId, Long audioId, Long folderId) {
        if (folderId == null) {
            // 获取或创建默认收藏夹
            Folder defaultFolder = folderMapper.selectOne(
                Wrappers.lambdaQuery(Folder.class)
                    .eq(Folder::getUserId, userId)
                    .eq(Folder::getName, "默认收藏夹")
            );
            if (defaultFolder == null) {
                defaultFolder = new Folder();
                defaultFolder.setUserId(userId);
                defaultFolder.setName("默认收藏夹");
                folderMapper.insert(defaultFolder);
            }
            folderId = defaultFolder.getId();
        }

        Wrapper<AudioFolderRelation> wrapper = Wrappers.lambdaQuery(AudioFolderRelation.class)
                .eq(AudioFolderRelation::getAudioId, audioId)
                .eq(AudioFolderRelation::getFolderId, folderId);
        if (audioFolderRelationMapper.selectOne(wrapper) != null) {
            throw new ConflictException("音频已存在收藏");
        }
        AudioFolderRelation audioFolderRelation = new AudioFolderRelation();
        audioFolderRelation.setAudioId(audioId);
        audioFolderRelation.setFolderId(folderId);
        audioFolderRelationMapper.insert(audioFolderRelation);
        folderMapper.incrementAudioCount(folderId, 1);
        audioInfoMapper.incrementCollectCount(audioId, 1);
    }

    @Override
    @Transactional
    public void uncollectAudio(Long userId, Long audioId, Long folderId) {
        if (folderId == null) {
            Folder defaultFolder = folderMapper.selectOne(
                Wrappers.lambdaQuery(Folder.class)
                    .eq(Folder::getUserId, userId)
                    .eq(Folder::getName, "默认收藏夹")
            );
            if (defaultFolder == null) {
                throw new BaseException(404, "默认收藏夹不存在");
            }
            folderId = defaultFolder.getId();
        }

        Wrapper<AudioFolderRelation> wrapper = Wrappers.lambdaQuery(AudioFolderRelation.class)
                .eq(AudioFolderRelation::getAudioId, audioId)
                .eq(AudioFolderRelation::getFolderId, folderId);
        if (audioFolderRelationMapper.selectOne(wrapper) == null) {
            throw new BaseException(404, "音频不存在收藏");
        }
        audioFolderRelationMapper.delete(wrapper);
        folderMapper.incrementAudioCount(folderId, -1);
        audioInfoMapper.incrementCollectCount(audioId, -1);
    }

    @Override
    public List<FolderVO> getAudioFolders(Long userId, Long audioId) {
        return audioFolderRelationMapper.selectAudioFolders(userId, audioId);
    }
}
