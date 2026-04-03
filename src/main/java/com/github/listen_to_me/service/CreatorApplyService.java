package com.github.listen_to_me.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.listen_to_me.domain.dto.CreatorApplyDTO;
import com.github.listen_to_me.domain.entity.CreatorApply;

public interface CreatorApplyService extends IService<CreatorApply> {
    void addCreatorApply(CreatorApplyDTO creatorApplyDTO);
}
