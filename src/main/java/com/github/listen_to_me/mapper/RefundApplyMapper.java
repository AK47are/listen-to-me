package com.github.listen_to_me.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.listen_to_me.domain.entity.RefundApply;
import com.github.listen_to_me.domain.vo.RefundApplyVO;

@Mapper
public interface RefundApplyMapper extends BaseMapper<RefundApply> {

    IPage<RefundApplyVO> selectRefundApplyPage(Page<RefundApplyVO> page, String status);

}
