package com.github.listen_to_me.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.listen_to_me.domain.entity.ConsultOrder;
import com.github.listen_to_me.domain.query.ConsultPageQuery;
import com.github.listen_to_me.domain.vo.ConsultOrderVO;

@Mapper
public interface ConsultOrderMapper extends BaseMapper<ConsultOrder> {

    IPage<ConsultOrderVO> selectUserConsultPage(Page<ConsultOrderVO> page, Long userId, ConsultPageQuery query);

    IPage<ConsultOrderVO> selectCreatorConsultPage(Page<ConsultOrderVO> page, Long creatorId, ConsultPageQuery query);
}
