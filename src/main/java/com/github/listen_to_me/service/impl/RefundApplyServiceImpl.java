package com.github.listen_to_me.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.listen_to_me.common.util.MinioUtils;
import com.github.listen_to_me.domain.entity.RefundApply;
import com.github.listen_to_me.domain.query.RefundPageQuery;
import com.github.listen_to_me.domain.vo.RefundApplyVO;
import com.github.listen_to_me.mapper.RefundApplyMapper;
import com.github.listen_to_me.service.IConsultOrderService;
import com.github.listen_to_me.service.IConsultSlotService;
import com.github.listen_to_me.service.IRefundApplyService;
import com.github.listen_to_me.service.ISysUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundApplyServiceImpl extends ServiceImpl<RefundApplyMapper, RefundApply>
        implements IRefundApplyService {

    private final IConsultOrderService iConsultOrderService;
    private final IConsultSlotService iConsultSlotService;
    private final ISysUserService iSysUserService;
    private final RefundApplyMapper refundApplyMapper;

    @Override
    public IPage<RefundApplyVO> getRefundApplyPage(RefundPageQuery query) {
        log.debug("分页查询退款申请 - 状态: {}", query.getStatus());
        Page<RefundApplyVO> page = new Page<>(query.getPageNum(), query.getPageSize());

        IPage<RefundApplyVO> result = refundApplyMapper.selectRefundApplyPage(page, query.getStatus());

        // 处理头像临时 URL
        result.getRecords().forEach(vo -> {
            if (!StrUtil.hasBlank(vo.getUserAvatar())) {
                try {
                    String avatarUrl = MinioUtils.getPresignedUrl(vo.getUserAvatar());
                    vo.setUserAvatar(avatarUrl);
                } catch (Exception e) {
                    log.warn("生成头像临时链接失败 - 路径: {}", vo.getUserAvatar(), e);
                    vo.setUserAvatar(null);
                }
            }
        });

        return result;
    }
}
