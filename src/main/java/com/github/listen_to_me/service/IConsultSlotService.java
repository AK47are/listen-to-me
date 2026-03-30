package com.github.listen_to_me.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.listen_to_me.domain.dto.SlotDTO;
import com.github.listen_to_me.domain.entity.ConsultSlot;

/**
 * <p>
 * 服务接口
 * </p>
 *
 * @author kun
 * @since 2026-03-24
 */
public interface IConsultSlotService extends IService<ConsultSlot> {

    /**
     * 批量生成时间槽
     * 
     * @param slotDTOList 时间槽列表
     */
    void saveSlotBatch(List<SlotDTO> slotDTOList);
}
