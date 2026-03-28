package com.github.listen_to_me.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.listen_to_me.domain.entity.AudioFolderRelation;
import com.github.listen_to_me.mapper.AudioFolderRelationMapper;
import com.github.listen_to_me.service.AudioFolderRelationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AudioFolderRelationServiceImpl extends ServiceImpl<AudioFolderRelationMapper, AudioFolderRelation> implements AudioFolderRelationService {

}
