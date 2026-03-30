package com.github.listen_to_me.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.listen_to_me.common.enumeration.RedisKey;
import com.github.listen_to_me.common.exception.BaseException;
import com.github.listen_to_me.common.util.MinioUtils;
import com.github.listen_to_me.common.util.RedisUtils;
import com.github.listen_to_me.common.util.SecurityUtils;
import com.github.listen_to_me.domain.dto.UserProfileUpdateDTO;
import com.github.listen_to_me.domain.entity.CoinTransaction;
import com.github.listen_to_me.domain.entity.SysUser;
import com.github.listen_to_me.domain.vo.BalanceVO;
import com.github.listen_to_me.domain.vo.UserVO;
import com.github.listen_to_me.mapper.SysUserMapper;
import com.github.listen_to_me.service.ICoinTransactionService;
import com.github.listen_to_me.service.ISysUserService;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private final PasswordEncoder passwordEncoder;
    private final ICoinTransactionService iCoinTransactionService;

    @Override
    public UserVO findProfile() {
        Long currId = SecurityUtils.getCurrentUserId();
        log.debug("查询详情 - ID: {}", currId);
        SysUser sysUser = this.getById(currId);
        try {
            sysUser.setAvatar(MinioUtils.getPresignedUrl(sysUser.getAvatar()));
        } catch (Exception e) {
            throw new BaseException("头像 url 获取异常！");
        }
        UserVO userVO = BeanUtil.copyProperties(sysUser, UserVO.class);
        log.debug("查询详情 - ID: {}, 用户信息: {}", currId, userVO);
        return userVO;
    }

    @Override
    public void modifyProfile(UserProfileUpdateDTO updateDTO) {
        Long currId = SecurityUtils.getCurrentUserId();
        SysUser sysUser = this.getById(currId);

        String newPhone = updateDTO.getPhone();
        String newEmail = updateDTO.getEmail();
        String oldPhone = sysUser.getPhone();
        String oldEmail = sysUser.getEmail();
        if (StrUtil.isBlank(newPhone) && StrUtil.isBlank(newEmail)) {
            throw new BaseException(400, "手机号和邮箱不能同时为空");
        }

        boolean needUpdatePhone = StrUtil.isNotBlank(newPhone)
                && !newPhone.equals(oldPhone);
        boolean needUpdateEmail = StrUtil.isNotBlank(newEmail)
                && !newEmail.equals(oldEmail);
        if (needUpdatePhone && needUpdateEmail) {
            throw new BaseException(400, "手机号和邮箱不能同时更新");
        }

        if (needUpdatePhone || needUpdateEmail) {
            String target = needUpdatePhone ? newPhone : newEmail;
            String cachedVerifyCode = RedisUtils.get(RedisKey.VERIFY_CODE, target);
            if (StrUtil.isBlank(cachedVerifyCode) || !cachedVerifyCode.equalsIgnoreCase(updateDTO.getVerifyCode())) {
                throw new BaseException(400, "校验码错误或已过期");
            }
            RedisUtils.delete(RedisKey.VERIFY_CODE, target);
        }

        boolean needUpdatePassword = StrUtil.isNotBlank(updateDTO.getNewPassword());
        if (needUpdatePassword && !passwordEncoder.matches(updateDTO.getOldPassword(), sysUser.getPassword())) {
            throw new BaseException(400, "原密码错误");
        }

        BeanUtil.copyProperties(updateDTO, sysUser);
        if (needUpdatePassword) {
            sysUser.setPassword(passwordEncoder.encode(updateDTO.getNewPassword()));
            log.debug("用户更新密码，原密码-{}，新密码-{}", updateDTO.getOldPassword(), updateDTO.getNewPassword());
        }
        // TODO: 用户头像 url 处理

        this.updateById(sysUser);
    }

    @Override
    public BalanceVO getBalanceStats(Long userId) {
        log.debug("查询用户余额统计 - 用户ID: {}", userId);

        SysUser user = getById(userId);
        if (user == null) {
            log.debug("用户不存在 - ID: {}", userId);
            throw new BaseException(404, "用户不存在");
        }

        BalanceVO vo = new BalanceVO();
        vo.setBalance(user.getBalance());

        // 查询累计充值
        LambdaQueryWrapper<CoinTransaction> rechargeWrapper = Wrappers.<CoinTransaction>lambdaQuery()
                .eq(CoinTransaction::getUserId, userId)
                .eq(CoinTransaction::getType, "INCOME")
                .eq(CoinTransaction::getBizType, "RECHARGE");
        BigDecimal totalRecharge = iCoinTransactionService.list(rechargeWrapper).stream()
                .map(CoinTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalRecharge(totalRecharge);

        // 查询累计消费
        LambdaQueryWrapper<CoinTransaction> expenseWrapper = Wrappers.<CoinTransaction>lambdaQuery()
                .eq(CoinTransaction::getUserId, userId)
                .eq(CoinTransaction::getType, "EXPENSE")
                .in(CoinTransaction::getBizType, List.of("AUDIO", "CONSULT"));
        BigDecimal totalSpent = iCoinTransactionService.list(expenseWrapper).stream()
                .map(CoinTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalSpent(totalSpent);

        return vo;
    }
}
