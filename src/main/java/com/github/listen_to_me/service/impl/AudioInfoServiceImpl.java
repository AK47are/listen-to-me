package com.github.listen_to_me.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.listen_to_me.domain.entity.AudioInfo;
import com.github.listen_to_me.domain.query.FavoriteQuery;
import com.github.listen_to_me.domain.vo.AudioVO;
import com.github.listen_to_me.mapper.AudioInfoMapper;
import com.github.listen_to_me.service.IAudioInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class AudioInfoServiceImpl extends ServiceImpl<AudioInfoMapper, AudioInfo> implements IAudioInfoService {
    private final AudioInfoMapper audioInfoMapper;

    @Override
    public IPage<AudioVO> getFavoriteAudioPage(FavoriteQuery favoriteQuery) {
        // 构建分页
        Page<AudioInfo> page = new Page<>(favoriteQuery.getPageNum(), favoriteQuery.getPageSize());

        // 直接调用 mapper 联表分页查询
        IPage<AudioInfo> audioPage = audioInfoMapper.selectAudioByFolderId(page, favoriteQuery.getFolderId());
        return audioPage.convert(audio -> BeanUtil.copyProperties(audio, AudioVO.class));
    }

}
