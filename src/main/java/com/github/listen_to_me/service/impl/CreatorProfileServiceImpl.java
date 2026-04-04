package com.github.listen_to_me.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.listen_to_me.common.util.MinioUtils;
import com.github.listen_to_me.domain.entity.CreatorProfile;
import com.github.listen_to_me.domain.query.CreatorPageQuery;
import com.github.listen_to_me.domain.vo.CreatorVO;
import com.github.listen_to_me.mapper.CreatorProfileMapper;
import com.github.listen_to_me.service.ICreatorProfileService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreatorProfileServiceImpl extends ServiceImpl<CreatorProfileMapper, CreatorProfile>
        implements ICreatorProfileService {

    @Override
    public IPage<CreatorVO> getCreatorPage(Long userId, CreatorPageQuery query) {
        log.debug("分页查询创作者列表 - 用户ID: {}, 关键词: {}", userId, query.getKeyword());
        Page<CreatorVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<CreatorVO> result = baseMapper.selectCreatorPage(page, userId, query);

        result.getRecords().forEach(vo -> vo.setAvatar(MinioUtils.getPresignedUrl(vo.getAvatar())));

        return result;
    }
}
