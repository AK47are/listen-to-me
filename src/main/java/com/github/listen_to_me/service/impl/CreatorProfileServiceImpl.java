package com.github.listen_to_me.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.listen_to_me.domain.entity.CreatorProfile;
import com.github.listen_to_me.mapper.CreatorProfileMapper;
import com.github.listen_to_me.service.ICreatorProfileService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreatorProfileServiceImpl extends ServiceImpl<CreatorProfileMapper, CreatorProfile>
        implements ICreatorProfileService {
}
