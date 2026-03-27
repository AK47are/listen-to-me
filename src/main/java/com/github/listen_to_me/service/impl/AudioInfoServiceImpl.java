package com.github.listen_to_me.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.listen_to_me.common.exception.BaseException;
import com.github.listen_to_me.common.util.MinioUtils;
import com.github.listen_to_me.domain.entity.AudioInfo;
import com.github.listen_to_me.domain.query.AudioQuery;
import com.github.listen_to_me.domain.vo.AudioTagVO;
import com.github.listen_to_me.domain.vo.AudioVO;
import com.github.listen_to_me.mapper.AudioInfoMapper;
import com.github.listen_to_me.mapper.SysTagMapper;
import com.github.listen_to_me.service.IAudioInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
public class AudioInfoServiceImpl extends ServiceImpl<AudioInfoMapper, AudioInfo> implements IAudioInfoService {

    private final SysTagMapper sysTagMapper;
    private final AudioInfoMapper audioInfoMapper;

    @Override
    public Page<AudioVO> findAudioPage(AudioQuery audioQuery) {
        Page<AudioVO> page = new Page<>(audioQuery.getPageNum(), audioQuery.getPageSize());
        Page<AudioVO> result = (Page<AudioVO>) audioInfoMapper.selectAudioPage(page, audioQuery);

        if (result.getRecords().isEmpty()) {
            return result;
        }

        List<Long> audioIds = result.getRecords().stream()
                .map(AudioVO::getId)
                .collect(Collectors.toList());
        List<AudioTagVO> audioTagList = sysTagMapper.selectTagsByAudioIds(audioIds);
        Map<Long, List<String>> tagsMap = audioTagList.stream()
                .collect(Collectors.groupingBy(
                        AudioTagVO::getAudioId,
                        Collectors.mapping(AudioTagVO::getTagName, Collectors.toList())
                ));

        result.getRecords().forEach(vo -> {
            List<String> tags = tagsMap.getOrDefault(vo.getId(), new ArrayList<>());
            vo.setTags(tags);
            try {
                vo.setCoverUrl(MinioUtils.getPresignedUrl(vo.getCoverUrl()));
            } catch (Exception e) {
                throw new BaseException("获取音频封面失败");
            }
        });
        return result;
    }
}
