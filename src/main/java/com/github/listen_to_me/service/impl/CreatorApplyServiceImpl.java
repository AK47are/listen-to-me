package com.github.listen_to_me.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.listen_to_me.common.exception.BaseException;
import com.github.listen_to_me.common.exception.ConflictException;
import com.github.listen_to_me.common.util.SecurityUtils;
import com.github.listen_to_me.domain.dto.CreatorApplyDTO;
import com.github.listen_to_me.domain.entity.CreatorApply;
import com.github.listen_to_me.domain.query.AuditQuery;
import com.github.listen_to_me.domain.vo.AuditApplyVO;
import com.github.listen_to_me.domain.vo.CreatorApplyVO;
import com.github.listen_to_me.mapper.CreatorApplyMapper;
import com.github.listen_to_me.service.CreatorApplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CreatorApplyServiceImpl extends ServiceImpl<CreatorApplyMapper, CreatorApply> implements CreatorApplyService {

    private final CreatorApplyMapper creatorApplyMapper;

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

    @Override
    public IPage<AuditApplyVO> findAuditApplyPage(AuditQuery query) {
        Page<CreatorApply> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<CreatorApply> iPage = creatorApplyMapper.selectPage(page, Wrappers.lambdaQuery(CreatorApply.class)
                .eq(CreatorApply::getStatus, query.getStatus()));
        IPage<AuditApplyVO> voPage = new Page<>(iPage.getCurrent(), iPage.getSize());
        voPage.setRecords(iPage.getRecords()
                .stream().map(creatorApply -> BeanUtil.copyProperties(creatorApply, AuditApplyVO.class))
                .collect(Collectors.toList()));
        return voPage;
    }
}
