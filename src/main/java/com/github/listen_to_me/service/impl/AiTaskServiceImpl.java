package com.github.listen_to_me.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.listen_to_me.domain.entity.AiTask;
import com.github.listen_to_me.mapper.AiTaskMapper;
import com.github.listen_to_me.service.IAiTaskService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiTaskServiceImpl extends ServiceImpl<AiTaskMapper, AiTask> implements IAiTaskService {
}
