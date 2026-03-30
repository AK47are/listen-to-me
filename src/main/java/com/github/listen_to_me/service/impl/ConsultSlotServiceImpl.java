package com.github.listen_to_me.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.listen_to_me.common.exception.BaseException;
import com.github.listen_to_me.common.exception.ConflictException;
import com.github.listen_to_me.common.util.SecurityUtils;
import com.github.listen_to_me.domain.dto.SlotDTO;
import com.github.listen_to_me.domain.entity.ConsultSlot;
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
