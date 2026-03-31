package com.github.listen_to_me.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.listen_to_me.domain.dto.RefundAuditDTO;
import com.github.listen_to_me.domain.entity.RefundApply;
import com.github.listen_to_me.domain.query.RefundPageQuery;
import com.github.listen_to_me.domain.vo.RefundApplyVO;

public interface IRefundApplyService extends IService<RefundApply> {

    /**
     * 分页查询退款申请
     * 
     * @param query 分页查询条件
     * @return 分页结果
     */
    IPage<RefundApplyVO> getRefundApplyPage(RefundPageQuery query);

    /**
     * 审核退款
     * 
     * @param auditDTO 审核请求
     */
    void auditRefund(RefundAuditDTO auditDTO);
}
