package com.github.listen_to_me.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.listen_to_me.domain.entity.CreatorApply;
import com.github.listen_to_me.mapper.CreatorApplyMapper;
import com.github.listen_to_me.service.CreatorApplyService;
import org.springframework.stereotype.Service;

@Service
public class CreatorApplyServiceImpl extends ServiceImpl<CreatorApplyMapper, CreatorApply> implements CreatorApplyService {
}
