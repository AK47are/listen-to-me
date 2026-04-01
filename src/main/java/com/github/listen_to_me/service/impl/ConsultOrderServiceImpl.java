package com.github.listen_to_me.service.impl;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.listen_to_me.common.exception.BaseException;
import com.github.listen_to_me.common.util.MinioUtils;
import com.github.listen_to_me.domain.dto.ConsultDTO;
import com.github.listen_to_me.domain.dto.RefundApplyDTO;
import com.github.listen_to_me.domain.entity.ConsultOrder;
import com.github.listen_to_me.domain.entity.ConsultSlot;
import com.github.listen_to_me.domain.entity.RefundApply;
import com.github.listen_to_me.domain.entity.SysUser;
import com.github.listen_to_me.domain.query.ConsultPageQuery;
import com.github.listen_to_me.domain.vo.ConsultOrderVO;
import com.github.listen_to_me.mapper.ConsultOrderMapper;
import com.github.listen_to_me.service.IConsultOrderService;
import com.github.listen_to_me.service.IConsultSlotService;
import com.github.listen_to_me.service.IRefundApplyService;
import com.github.listen_to_me.service.ISysUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultOrderServiceImpl extends ServiceImpl<ConsultOrderMapper, ConsultOrder>
        implements IConsultOrderService {

    private final IConsultSlotService consultSlotService;
    private final ISysUserService sysUserService;

    @Lazy
    @Autowired
    // NOTE: 使用 @Lazy 注解打破与 RefundApplyServiceImpl 的循环依赖
    private IRefundApplyService refundApplyService;

    @Override
    @Transactional
    public ConsultOrderVO saveConsult(Long userId, ConsultDTO consultDTO) {
        log.debug("发起预约 - 用户ID: {}, 时间槽ID: {}", userId, consultDTO.getSlotId());

        // 校验时间槽
        ConsultSlot slot = consultSlotService.getById(consultDTO.getSlotId());
        if (slot == null) {
            throw new BaseException(404, "时间槽不存在");
        }
        if (!"AVAILABLE".equals(slot.getStatus())) {
            throw new BaseException(400, "时间槽不可用");
        }
        if (slot.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BaseException(400, "无法预约已开始或已结束的时间槽");
        }

        // 创建预约订单
        ConsultOrder order = new ConsultOrder();
        order.setSlotId(consultDTO.getSlotId());
        order.setUserId(userId);
        order.setCreatorId(slot.getCreatorId());
        order.setMessage(consultDTO.getMessage());
        order.setStatus("PENDING_CONFIRM");
        order.setPayAmount(slot.getPrice());
        save(order);

        // 扣减余额
        boolean deducted = sysUserService.deductBalance(userId, slot.getPrice(), "CONSULT",
                String.valueOf(order.getId()));
        if (!deducted) {
            throw new BaseException(400, "余额不足");
        }

        // 更新时间槽状态为 BOOKED
        slot.setStatus("BOOKED");
        consultSlotService.updateById(slot);

        log.debug("发起预约成功 - 订单ID: {}", order.getId());

        // 查询创作者信息
        SysUser creator = sysUserService.getById(slot.getCreatorId());

        String creatorName = null;
        String creatorAvatar = null;
        if (creator != null) {
            creatorName = creator.getNickname();
            if (creator.getAvatar() != null && !creator.getAvatar().isBlank()) {
                try {
                    creatorAvatar = MinioUtils.getPresignedUrl(creator.getAvatar());
                } catch (Exception e) {
                    log.warn("生成头像临时链接失败 - 路径: {}", creator.getAvatar(), e);
                }
            }
        }

        // 组装返回 VO
        ConsultOrderVO vo = new ConsultOrderVO();
        vo.setId(order.getId());
        vo.setSlotId(order.getSlotId());
        vo.setStartTime(slot.getStartTime());
        vo.setEndTime(slot.getEndTime());
        vo.setPrice(slot.getPrice());
        vo.setStatus(order.getStatus());
        vo.setAddress(null);
        vo.setCreatorId(slot.getCreatorId());
        vo.setCreatorName(creatorName);
        vo.setCreatorAvatar(creatorAvatar);
        vo.setMessage(order.getMessage());
        vo.setCreateTime(order.getCreateTime());
        return vo;
    }

    @Override
    public IPage<ConsultOrderVO> getUserConsultPage(Long userId, ConsultPageQuery query) {
        log.debug("分页查询预约订单 - 用户ID: {}, 状态: {}", userId, query.getStatus());
        Page<ConsultOrderVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<ConsultOrderVO> result = baseMapper.selectConsultPage(page, userId, query.getStatus());

        // 处理头像临时 URL
        result.getRecords().forEach(vo -> {
            if (vo.getCreatorAvatar() != null && !vo.getCreatorAvatar().isBlank()) {
                try {
                    String avatarUrl = MinioUtils.getPresignedUrl(vo.getCreatorAvatar());
                    vo.setCreatorAvatar(avatarUrl);
                } catch (Exception e) {
                    log.warn("生成头像临时链接失败 - 路径: {}", vo.getCreatorAvatar(), e);
                    vo.setCreatorAvatar(null);
                }
            }
        });

        return result;
    }

    @Override
    @Transactional
    public void cancelConsult(Long userId, Long orderId) {
        log.debug("取消预约 - 用户ID: {}, 订单ID: {}", userId, orderId);

        // 校验订单存在且属于当前用户
        ConsultOrder order = getById(orderId);
        if (order == null) {
            throw new BaseException(404, "订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BaseException(404, "订单不存在");
        }

        // 校验订单状态
        if (!"PENDING_CONFIRM".equals(order.getStatus())) {
            throw new BaseException(400, "当前状态无法取消");
        }

        // 退还余额
        boolean added = sysUserService.addBalance(userId, order.getPayAmount(), "REFUND", String.valueOf(orderId));
        if (!added) {
            throw new BaseException("退还余额失败");
        }

        // 更新订单状态为 CANCELLED
        order.setStatus("CANCELLED");
        updateById(order);

        // 释放时间槽状态为 AVAILABLE
        ConsultSlot slot = consultSlotService.getById(order.getSlotId());
        if (slot != null) {
            slot.setStatus("AVAILABLE");
            consultSlotService.updateById(slot);
        }

        log.debug("取消预约成功 - 订单ID: {}", orderId);
    }

    @Override
    @Transactional
    public void applyRefund(Long userId, Long orderId, RefundApplyDTO refundApplyDTO) {
        log.debug("申请退款 - 用户ID: {}, 订单ID: {}, 原因: {}", userId, orderId, refundApplyDTO);

        // 校验订单存在且属于当前用户
        ConsultOrder order = getById(orderId);
        if (order == null) {
            throw new BaseException(404, "订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BaseException(404, "订单不存在");
        }

        // 校验订单状态
        String status = order.getStatus();
        if (!"CONFIRMED".equals(status) && !"COMPLETED".equals(status)) {
            throw new BaseException(400, "当前状态无法申请退款");
        }

        // 检查是否已有进行中的退款申请
        LambdaQueryWrapper<RefundApply> wrapper = Wrappers.<RefundApply>lambdaQuery()
                .eq(RefundApply::getOrderId, orderId)
                .eq(RefundApply::getStatus, "PENDING");
        long count = refundApplyService.count(wrapper);
        if (count > 0) {
            throw new BaseException(400, "已有退款申请处理中");
        }

        // 创建退款申请记录
        RefundApply apply = new RefundApply();
        apply.setOrderId(orderId);
        apply.setUserId(userId);
        apply.setReason(refundApplyDTO.getReason());
        apply.setStatus("PENDING");
        refundApplyService.save(apply);

        // 更新订单状态为 REFUND_PENDING
        order.setStatus("REFUND_PENDING");
        updateById(order);

        log.debug("申请退款成功 - 订单ID: {}, 退款申请ID: {}", orderId, apply.getId());
    }

}
