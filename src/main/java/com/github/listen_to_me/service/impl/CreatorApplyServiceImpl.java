package com.github.listen_to_me.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.listen_to_me.common.exception.BaseException;
import com.github.listen_to_me.common.exception.ConflictException;
import com.github.listen_to_me.common.util.SecurityUtils;
import com.github.listen_to_me.domain.dto.CreatorApplyDTO;
import com.github.listen_to_me.domain.entity.CreatorApply;
import com.github.listen_to_me.domain.vo.CreatorApplyVO;
import com.github.listen_to_me.mapper.CreatorApplyMapper;
import com.github.listen_to_me.service.CreatorApplyService;
import org.springframework.stereotype.Service;


@Service
public class CreatorApplyServiceImpl extends ServiceImpl<CreatorApplyMapper, CreatorApply> implements CreatorApplyService {

    @Override
    public void addCreatorApply(CreatorApplyDTO creatorApplyDTO) {
        Long currId = SecurityUtils.getCurrentUserId();
        CreatorApply creatorApply = getOne(Wrappers.lambdaQuery(CreatorApply.class)
                .eq(CreatorApply::getUserId, currId));
        if(creatorApply != null && "APPROVED".equals(creatorApply.getStatus())){
            throw new BaseException(400,"您已是创作者");
        }
        if (creatorApply != null && "PENDING".equals(creatorApply.getStatus())){
            throw new ConflictException("已有申请正在审核中");
        }
        if (creatorApply == null) {
            creatorApply = new CreatorApply();
        }
        BeanUtil.copyProperties(creatorApplyDTO, creatorApply);
        creatorApply.setUserId(currId);
        // 被拒绝过的话，重新提交申请，状态为PENDING
        creatorApply.setStatus("PENDING");
        saveOrUpdate(creatorApply);
    }

    @Override
    public CreatorApplyVO findApplyStatus() {
        Long currId = SecurityUtils.getCurrentUserId();
        CreatorApply creatorApply = getOne(Wrappers.lambdaQuery(CreatorApply.class)
                .eq(CreatorApply::getUserId, currId));
        if(creatorApply == null){
            throw new BaseException(400,"您未提交创作者申请");
        }
        return BeanUtil.copyProperties(creatorApply, CreatorApplyVO.class);
    }
}
