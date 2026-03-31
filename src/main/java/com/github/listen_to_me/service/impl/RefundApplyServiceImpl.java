package com.github.listen_to_me.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.listen_to_me.common.exception.BaseException;
import com.github.listen_to_me.common.util.MinioUtils;
import com.github.listen_to_me.domain.dto.RefundAuditDTO;
import com.github.listen_to_me.domain.entity.ConsultOrder;
import com.github.listen_to_me.domain.entity.ConsultSlot;
import com.github.listen_to_me.domain.entity.RefundApply;
import com.github.listen_to_me.domain.query.RefundPageQuery;
import com.github.listen_to_me.domain.vo.RefundApplyVO;
import com.github.listen_to_me.mapper.RefundApplyMapper;
import com.github.listen_to_me.service.IConsultOrderService;
import com.github.listen_to_me.service.IConsultSlotService;
import com.github.listen_to_me.service.IRefundApplyService;
import com.github.listen_to_me.service.ISysUserService;

import cn.hutool.core.util.StrUtil;
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

    @Override
    @Transactional
    public void auditRefund(RefundAuditDTO auditDTO) {
        log.debug("审核退款 - 申请ID: {}, 通过: {}", auditDTO.getApplyId(), auditDTO.getApproved());

        // 查询退款申请
        RefundApply apply = getById(auditDTO.getApplyId());
        if (apply == null) {
            throw new BaseException(404, "退款申请不存在");
        }
        if (!"PENDING".equals(apply.getStatus())) {
            throw new BaseException(400, "退款申请已处理");
        }

        // 查询关联订单
        ConsultOrder order = iConsultOrderService.getById(apply.getOrderId());
        if (order == null) {
            throw new BaseException(404, "关联订单不存在");
        }

        if (auditDTO.getApproved()) {
            // 通过：退还余额
            boolean added = iSysUserService.addBalance(apply.getUserId(), order.getPayAmount(), "REFUND",
                    String.valueOf(order.getId()));
            if (!added) {
                throw new BaseException("退还余额失败");
            }
            order.setStatus("REFUNDED");
            iConsultOrderService.updateById(order);
            iConsultSlotService.update(Wrappers.<ConsultSlot>lambdaUpdate()
                    .eq(ConsultSlot::getId, order.getSlotId())
                    .set(ConsultSlot::getStatus, "AVAILABLE"));

            apply.setStatus("PROCESSED");
            updateById(apply);
            log.debug("退款审核通过 - 申请ID: {}, 订单ID: {}", auditDTO.getApplyId(), order.getId());
        } else {
            // 拒绝：校验拒绝原因
            if (StrUtil.hasBlank(auditDTO.getRejectReason())) {
                throw new BaseException(400, "拒绝时必须填写原因");
            }
            // 默认设置为 CONFIRMED
            order.setStatus("CONFIRMED");
            iConsultOrderService.updateById(order);
            apply.setStatus("PROCESSED");
            apply.setRejectReason(auditDTO.getRejectReason());
            updateById(apply);
            log.debug("退款审核拒绝 - 申请ID: {}, 原因: {}", auditDTO.getApplyId(), auditDTO.getRejectReason());
        }
    }

}
