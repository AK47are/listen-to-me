package com.github.listen_to_me.service;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.listen_to_me.domain.dto.SlotDTO;
import com.github.listen_to_me.domain.entity.ConsultSlot;
import com.github.listen_to_me.domain.query.SlotPageQuery;
import com.github.listen_to_me.domain.vo.SlotVO;

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

    /**
     * 分页查询时间槽，创作者端使用，可返回 address
     * 
     * @param query 分页查询条件
     * @return 分页结果
     */
    IPage<SlotVO> getCreatorSlotPage(SlotPageQuery query);

    /**
     * 修改时间槽状态
     * 
     * @param slotId 时间槽ID
     * @param status 目标状态 (AVAILABLE / CANCELLED)
     */
    void updateSlotStatus(Long slotId, String status);
}
