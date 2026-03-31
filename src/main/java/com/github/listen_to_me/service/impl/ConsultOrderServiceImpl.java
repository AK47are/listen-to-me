package com.github.listen_to_me.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.listen_to_me.common.exception.BaseException;
import com.github.listen_to_me.common.util.MinioUtils;
import com.github.listen_to_me.domain.dto.ConsultDTO;
import com.github.listen_to_me.domain.entity.ConsultOrder;
import com.github.listen_to_me.domain.entity.ConsultSlot;
import com.github.listen_to_me.domain.entity.SysUser;
import com.github.listen_to_me.domain.vo.ConsultOrderVO;
import com.github.listen_to_me.mapper.ConsultOrderMapper;
import com.github.listen_to_me.service.IConsultOrderService;
import com.github.listen_to_me.service.IConsultSlotService;
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
}
