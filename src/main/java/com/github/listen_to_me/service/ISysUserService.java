package com.github.listen_to_me.service;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.listen_to_me.domain.dto.RechargeResultDTO;
import com.github.listen_to_me.domain.dto.UserProfileUpdateDTO;
import com.github.listen_to_me.domain.entity.SysUser;
import com.github.listen_to_me.domain.query.UserPageQuery;
import com.github.listen_to_me.domain.vo.BalanceVO;
import com.github.listen_to_me.domain.vo.RechargeResultVO;
import com.github.listen_to_me.domain.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

public interface ISysUserService extends IService<SysUser> {

    UserVO findProfile();

    void modifyProfile(UserProfileUpdateDTO updateDTO);

    /**
     * 获取用户余额统计信息
     * 
     * @param userId 用户ID
     * @return 余额统计VO
     */
    BalanceVO getBalanceStats(Long userId);

    /**
     * 扣减当前用户余额
     * 
     * 使用场景：
     * - 购买音频
     * - 发起咨询预约
     * - 管理员审核提现通过
     *
     * 流程：
     * 1. 校验数据有效性
     * 2. 记录到虚拟币流水
     * 
     * @param userId  用户ID
     * @param amount  扣减金额
     * @param bizType 业务类型
     * @param bizId   业务ID
     * @return 是否扣减成功
     */
    boolean deductBalance(Long userId, BigDecimal amount, String bizType, String bizId);

    /**
     * 增加用户余额
     * 
     * 使用场景：
     * - 充值回调
     * - 取消预约退还余额
     * - 创作者拒绝预约退还余额
     * - 管理员审核退款通过
     * 
     * @param userId  用户ID
     * @param amount  增加金额（必须大于0）
     * @param bizType 业务类型：RECHARGE(充值) / REFUND(退款)
     * @param bizId   业务ID（充值订单号/退款单号）
     * @return true-增加成功
     */
    boolean addBalance(Long userId, BigDecimal amount, String bizType, String bizId);

    /**
     * 封禁用户
     * 
     * @param userId 用户ID
     */
    void banUser(Long userId);

    /**
     * 解封用户
     * 
     * @param userId 用户ID
     */
    void unbanUser(Long userId);

    /**
     * 分页查询用户
     * 可根据username查询用户
     * @param query 分页查询条件
     * @return 分页结果
     */
    IPage<UserVO> getUserPage(UserPageQuery query);

    RechargeResultVO recharge(RechargeResultDTO rechargeResultDTO) throws Exception;

}
