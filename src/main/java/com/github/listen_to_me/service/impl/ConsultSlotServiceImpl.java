package com.github.listen_to_me.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.listen_to_me.common.exception.BaseException;
import com.github.listen_to_me.common.exception.ConflictException;
import com.github.listen_to_me.common.util.SecurityUtils;
import com.github.listen_to_me.domain.dto.SlotDTO;
import com.github.listen_to_me.domain.entity.ConsultSlot;
import com.github.listen_to_me.domain.query.SlotPageQuery;
import com.github.listen_to_me.domain.vo.SlotVO;
import com.github.listen_to_me.mapper.ConsultSlotMapper;
import com.github.listen_to_me.service.IConsultSlotService;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author kun
 * @since 2026-03-24
 */
@Slf4j
@Service
public class ConsultSlotServiceImpl extends ServiceImpl<ConsultSlotMapper, ConsultSlot> implements IConsultSlotService {

    @Override
    @Transactional
    public void saveSlotBatch(List<SlotDTO> slotDTOList) {
        Long creatorId = SecurityUtils.getCurrentUserId();
        log.debug("批量生成时间槽 - 创作者ID: {}, 数量: {}", creatorId, slotDTOList.size());

        // 校验时间顺序业务规则
        for (SlotDTO dto : slotDTOList) {
            if (!dto.getStartTime().isBefore(dto.getEndTime())) {
                log.debug("时间槽时间无效 - 开始时间: {}, 结束时间: {}", dto.getStartTime(), dto.getEndTime());
                throw new BaseException(400, "开始时间必须早于结束时间");
            }
        }

        List<ConsultSlot> newSlotList = slotDTOList.stream()
                .map(dto -> {
                    ConsultSlot slot = new ConsultSlot();
                    slot.setCreatorId(creatorId);
                    slot.setStartTime(dto.getStartTime());
                    slot.setEndTime(dto.getEndTime());
                    slot.setPrice(dto.getPrice());
                    slot.setAddress(dto.getAddress());
                    slot.setStatus("AVAILABLE");
                    return slot;
                })
                .collect(Collectors.toList());

        checkConflictBatch(newSlotList);

        boolean success = saveBatch(newSlotList);
        if (!success) {
            log.error("批量生成时间槽失败 - 创作者ID: {}", creatorId);
            throw new BaseException("批量生成时间槽失败");
        }

        log.debug("批量生成时间槽成功 - 创作者ID: {}, 数量: {}", creatorId, newSlotList.size());
    }

    @Override
    public IPage<SlotVO> getCreatorSlotPage(SlotPageQuery query) {
        Long creatorId = SecurityUtils.getCurrentUserId();
        log.debug("创作者端分页查询时间槽 - 创作者ID: {}, 开始时间: {}, 结束时间: {}",
                creatorId, query.getStartTime(), query.getEndTime());

        LambdaQueryWrapper<ConsultSlot> wrapper = Wrappers.<ConsultSlot>lambdaQuery()
                .eq(ConsultSlot::getCreatorId, creatorId);

        if (query.getStartTime() != null) {
            wrapper.ge(ConsultSlot::getStartTime, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le(ConsultSlot::getEndTime, query.getEndTime());
        }
        wrapper.orderByDesc(ConsultSlot::getStartTime);

        Page<ConsultSlot> slotPage = page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);

        return slotPage.convert(slot -> {
            SlotVO vo = new SlotVO();
            vo.setId(slot.getId());
            vo.setStartTime(slot.getStartTime());
            vo.setEndTime(slot.getEndTime());
            vo.setPrice(slot.getPrice());
            vo.setAddress(slot.getAddress());
            vo.setStatus(slot.getStatus());
            return vo;
        });
    }

    @Override
    @Transactional
    public void updateSlotStatus(Long slotId, String status) {
        Long creatorId = SecurityUtils.getCurrentUserId();
        log.debug("修改时间槽状态 - 时间槽ID: {}, 目标状态: {}", slotId, status);

        // 校验目标状态是否合法
        if (!"AVAILABLE".equals(status) && !"CANCELLED".equals(status)) {
            log.debug("非法状态 - 状态: {}", status);
            throw new BaseException(400, "状态值无效，仅支持 AVAILABLE 或 CANCELLED");
        }

        // 校验时间槽是否存在且属于当前用户
        ConsultSlot slot = getById(slotId);
        if (slot == null) {
            log.debug("时间槽不存在 - ID: {}", slotId);
            throw new BaseException(404, "时间槽不存在");
        }
        if (!slot.getCreatorId().equals(creatorId)) {
            log.debug("无权限修改 - 时间槽ID: {}, 当前用户: {}", slotId, creatorId);
            throw new BaseException(404, "时间槽不存在");
        }

        // 校验状态是否可修改
        String currentStatus = slot.getStatus();
        if ("BOOKED".equals(currentStatus) || "EXPIRED".equals(currentStatus)) {
            log.debug("时间槽状态不可修改 - 当前状态: {}, ID: {}", currentStatus, slotId);
            throw new BaseException(400, "当前状态无法修改");
        }

        // 更新状态
        slot.setStatus(status);
        boolean success = updateById(slot);
        if (!success) {
            log.error("修改时间槽状态失败 - ID: {}", slotId);
            throw new BaseException("修改时间槽状态失败");
        }

        log.debug("修改时间槽状态成功 - ID: {}, 旧状态: {}, 新状态: {}", slotId, currentStatus, status);
    }

    /**
     * 批量检查时间槽是否与已有槽位冲突
     */
    private void checkConflictBatch(List<ConsultSlot> newSlotList) {
        if (newSlotList == null || newSlotList.isEmpty()) {
            return;
        }

        Long creatorId = newSlotList.get(0).getCreatorId();

        LambdaQueryWrapper<ConsultSlot> wrapper = Wrappers.<ConsultSlot>lambdaQuery()
                .eq(ConsultSlot::getCreatorId, creatorId)
                .and(w -> {
                    for (ConsultSlot slot : newSlotList) {
                        w.or(sub -> sub
                                .lt(ConsultSlot::getStartTime, slot.getEndTime())
                                .gt(ConsultSlot::getEndTime, slot.getStartTime()));
                    }
                });

        long conflictCount = count(wrapper);
        if (conflictCount > 0) {
            log.debug("时间槽冲突 - 数量: {}", conflictCount);
            throw new ConflictException("时间槽与已有槽位冲突");
        }
    }
}
