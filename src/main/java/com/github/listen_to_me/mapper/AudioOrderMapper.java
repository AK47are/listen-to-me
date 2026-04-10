package com.github.listen_to_me.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.listen_to_me.domain.entity.AudioOrder;
import com.github.listen_to_me.domain.vo.AudioOrderVO;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author kun
 * @since 2026-03-24
 */
public interface AudioOrderMapper extends BaseMapper<AudioOrder> {

    IPage<AudioOrderVO> selectPageByUserId(IPage<AudioOrderVO> page, Long userId);
}
