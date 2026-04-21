package com.github.listen_to_me.service.impl;

import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.alipay.easysdk.factory.Factory;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.listen_to_me.common.config.AlipayConfig;
import com.github.listen_to_me.common.enumeration.RedisKey;
import com.github.listen_to_me.common.exception.BaseException;
import com.github.listen_to_me.common.util.MinioUtils;
import com.github.listen_to_me.common.util.RedisUtils;
import com.github.listen_to_me.domain.dto.RechargeResultDTO;
import com.github.listen_to_me.domain.dto.UserProfileUpdateDTO;
import com.github.listen_to_me.domain.entity.CoinTransaction;
import com.github.listen_to_me.domain.entity.SysUser;
import com.github.listen_to_me.domain.entity.UserRechargeOrder;
import com.github.listen_to_me.domain.query.RechargeOrderQuery;
import com.github.listen_to_me.domain.query.TransactionPageQuery;
import com.github.listen_to_me.domain.query.UserPageQuery;
import com.github.listen_to_me.domain.vo.BalanceVO;
import com.github.listen_to_me.domain.vo.CoinTransactionVO;
import com.github.listen_to_me.domain.vo.RechargeOrderVO;
import com.github.listen_to_me.domain.vo.RechargeResultVO;
import com.github.listen_to_me.domain.vo.UserVO;
import com.github.listen_to_me.mapper.CoinTransactionMapper;
import com.github.listen_to_me.mapper.SysUserMapper;
import com.github.listen_to_me.mapper.UserRechargeOrderMapper;
import com.github.listen_to_me.service.ICoinTransactionService;
import com.github.listen_to_me.service.ISysUserService;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private final PasswordEncoder passwordEncoder;
    private final SysUserMapper sysUserMapper;
    private final ICoinTransactionService iCoinTransactionService;
    private final AlipayConfig alipayConfig;
    private final UserRechargeOrderMapper userRechargeOrderMapper;
    private final CoinTransactionMapper coinTransactionMapper;

    @Override
    public String uploadAvatar(MultipartFile avatarFile) throws Exception {
        log.debug("上传用户头像 - 文件名: {}, 大小: {}", avatarFile.getOriginalFilename(), avatarFile.getSize());

        // 1. 文件类型校验
        String fileType = FileTypeUtil.getType(avatarFile.getInputStream());
        if (fileType == null || !"jpg".equals(fileType) && !"jpeg".equals(fileType) && !"png".equals(fileType)) {
            throw new BaseException(400, "仅支持上传 JPG、JPEG、PNG 格式头像");
        }

        // 2. 上传到 Minio（temp 目录）
        String objectName = MinioUtils.uploadFile(avatarFile, "temp", avatarFile.getOriginalFilename());

        // 3. 生成临时访问 URL
        String tempUrl = MinioUtils.getPresignedUrl(objectName);

        // 4. Base64 编码临时 URL
        String tempUrlBase64 = Base64.encode(tempUrl);

        // 5. 存储到 Redis：key=temp:avatar: + base64(tempUrl), value=objectName
        RedisUtils.set(RedisKey.TEMP_AVATAR_URL, tempUrlBase64, objectName);

        log.debug("上传头像成功 - objectName: {}, tempUrl: {}", objectName, tempUrl);
        return tempUrl;
    }

    @Override
    public UserVO findProfile(Long userId) {
        log.debug("查询详情 - ID: {}", userId);
        SysUser sysUser = this.getById(userId);
        sysUser.setAvatar(MinioUtils.getPresignedUrl(sysUser.getAvatar()));
        UserVO userVO = BeanUtil.copyProperties(sysUser, UserVO.class);
        log.debug("查询详情 - ID: {}, 用户信息: {}", userId, userVO);
        return userVO;
    }

    @Override
    @Transactional
    public void modifyProfile(Long userId, UserProfileUpdateDTO updateDTO) {
        log.debug("更新个人资料 - 开始处理，用户ID: {}", userId);

        SysUser sysUser = this.getById(userId);
        if (sysUser == null) {
            log.error("用户不存在 - ID: {}", userId);
            throw new BaseException("用户不存在");
        }

        String newPhone = updateDTO.getPhone();
        String newEmail = updateDTO.getEmail();
        String oldPhone = sysUser.getPhone();
        String oldEmail = sysUser.getEmail();

        boolean needUpdatePhone = newPhone != null && !newPhone.equals(oldPhone);
        boolean needUpdateEmail = newEmail != null && !newEmail.equals(oldEmail);

        if (needUpdatePhone && needUpdateEmail) {
            log.debug("手机号和邮箱不能同时更新 - 用户ID: {}", userId);
            throw new BaseException(400, "手机号和邮箱不能同时更新");
        }

        if (needUpdatePhone || needUpdateEmail) {
            String target = needUpdatePhone ? newPhone : newEmail;
            String cachedVerifyCode = RedisUtils.get(RedisKey.VERIFY_CODE, target);

            if (StrUtil.isBlank(cachedVerifyCode) || !cachedVerifyCode.equalsIgnoreCase(updateDTO.getVerifyCode())) {
                log.debug("验证码错误或已过期 - 目标: {}, 用户ID: {}", target, userId);
                throw new BaseException(400, "验证码错误或已过期");
            }
            RedisUtils.delete(RedisKey.VERIFY_CODE, target);
            log.debug("验证码验证成功 - 目标: {}, 用户ID: {}", target, userId);
        }

        boolean needUpdatePassword = StrUtil.isNotBlank(updateDTO.getNewPassword());
        if (needUpdatePassword) {
            if (StrUtil.isBlank(updateDTO.getOldPassword())) {
                log.debug("修改密码需要提供原密码 - 用户ID: {}", userId);
                throw new BaseException(400, "修改密码需要提供原密码");
            }

            if (!passwordEncoder.matches(updateDTO.getOldPassword(), sysUser.getPassword())) {
                log.debug("原密码错误 - 用户ID: {}", userId);
                throw new BaseException(400, "原密码错误");
            }

            sysUser.setPassword(passwordEncoder.encode(updateDTO.getNewPassword()));
            log.debug("密码更新成功 - 用户ID: {}", userId);
        }

        if (needUpdatePhone) {
            sysUser.setPhone(newPhone);
        }
        if (needUpdateEmail) {
            sysUser.setEmail(newEmail);
        }
        if (StrUtil.isNotBlank(updateDTO.getNickname())) {
            sysUser.setNickname(updateDTO.getNickname());
        }

        String avatarUrl = updateDTO.getAvatar();
        if (avatarUrl != null) {
            String tempUrlBase64 = Base64.encode(avatarUrl);
            String objectName = RedisUtils.get(RedisKey.TEMP_AVATAR_URL, tempUrlBase64);

            if (StrUtil.isBlank(objectName)) {
                log.error("无法获取头像的objectName - 用户ID: {}, URL: {}", userId, avatarUrl);
                throw new BaseException("头像数据异常，请重新上传头像");
            }

            try {
                String newObjectName = MinioUtils.copyFile(objectName, "avatar");
                MinioUtils.removeFile(objectName);
                sysUser.setAvatar(newObjectName);
                RedisUtils.delete(RedisKey.TEMP_AVATAR_URL, tempUrlBase64);
                log.debug("头像从临时目录移动到正式目录 - 用户ID: {}, 新路径: {}", userId, newObjectName);
            } catch (Exception e) {
                log.error("移动头像文件失败 - 用户ID: {}, 原路径: {}", userId, objectName, e);
                throw new BaseException("头像处理失败，请重试");
            }
        }
        this.updateById(sysUser);
        log.debug("个人资料更新完成 - 用户ID: {}", userId);
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

    @Override
    @Transactional
    public boolean deductBalance(Long userId, BigDecimal amount, String bizType, String bizId) {
        log.debug("扣减余额 - 用户ID: {}, 金额: {}, 业务类型: {}, 业务ID: {}", userId, amount, bizType, bizId);

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BaseException(400, "扣减金额必须大于0");
        }

        // 查询扣减前余额
        SysUser user = getById(userId);
        if (user == null) {
            throw new BaseException(404, "用户不存在");
        }

        BigDecimal balanceBefore = user.getBalance();

        if (balanceBefore.compareTo(amount) < 0) {
            log.debug("扣减余额失败 - 用户ID: {}, 余额不足: {}, 需要: {}", userId, balanceBefore, amount);
            return false;
        }

        // 扣减余额
        LambdaUpdateWrapper<SysUser> wrapper = Wrappers.<SysUser>lambdaUpdate()
                .eq(SysUser::getId, userId)
                .ge(SysUser::getBalance, amount)
                .setSql("balance = balance - " + amount);

        int updated = sysUserMapper.update(wrapper);
        if (updated == 0) {
            log.debug("扣减余额失败 - 用户ID: {}, 金额: {}", userId, amount);
            return false;
        }

        BigDecimal balanceAfter = balanceBefore.subtract(amount);

        // 记录流水
        CoinTransaction transaction = new CoinTransaction();
        transaction.setUserId(userId);
        transaction.setType("EXPENSE");
        transaction.setBizType(bizType);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setBizId(bizId);
        iCoinTransactionService.save(transaction);

        log.debug("扣减余额成功 - 用户ID: {}, 金额: {}, 变动后余额: {}", userId, amount, balanceAfter);
        return true;
    }

    @Override
    @Transactional
    public boolean addBalance(Long userId, BigDecimal amount, String bizType, String bizId) {
        log.debug("增加余额 - 用户ID: {}, 金额: {}, 业务类型: {}, 业务ID: {}", userId, amount, bizType, bizId);

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BaseException(400, "增加金额必须大于0");
        }

        // 查询增加前余额
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BaseException(404, "用户不存在");
        }
        BigDecimal balanceBefore = user.getBalance();

        // 增加余额
        LambdaUpdateWrapper<SysUser> wrapper = Wrappers.<SysUser>lambdaUpdate()
                .eq(SysUser::getId, userId)
                .setSql("balance = balance + " + amount);

        boolean updated = update(wrapper);
        if (!updated) {
            log.error("增加余额失败 - 用户ID: {}", userId);
            throw new BaseException("增加余额失败");
        }

        BigDecimal balanceAfter = balanceBefore.add(amount);

        // 记录流水
        CoinTransaction transaction = new CoinTransaction();
        transaction.setUserId(userId);
        transaction.setType("INCOME");
        transaction.setBizType(bizType);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setBizId(bizId);
        iCoinTransactionService.save(transaction);

        log.debug("增加余额成功 - 用户ID: {}, 金额: {}, 变动后余额: {}", userId, amount, balanceAfter);
        return true;
    }

    @Override
    public void banUser(Long userId) {
        log.debug("封禁用户 - 用户ID: {}", userId);

        SysUser user = getById(userId);
        if (user == null) {
            throw new BaseException(404, "用户不存在");
        }
        if ("BANNED".equals(user.getStatus())) {
            throw new BaseException(400, "用户已是封禁状态");
        }

        user.setStatus("BANNED");
        updateById(user);

        log.debug("封禁用户成功 - 用户ID: {}", userId);
    }

    @Override
    public void unbanUser(Long userId) {
        log.debug("解封用户 - 用户ID: {}", userId);

        SysUser user = getById(userId);
        if (user == null) {
            throw new BaseException(404, "用户不存在");
        }
        if ("NORMAL".equals(user.getStatus())) {
            throw new BaseException(400, "用户已是正常状态");
        }

        user.setStatus("NORMAL");
        updateById(user);

        log.debug("解封用户成功 - 用户ID: {}", userId);
    }

    @Override
    public IPage<UserVO> getUserPage(UserPageQuery query) {
        Page<SysUser> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<SysUser> wrapper = Wrappers.<SysUser>lambdaQuery();
        if (StrUtil.isNotBlank(query.getUsername())) {
            wrapper.like(SysUser::getUsername, query.getUsername());
        }
        Page<SysUser> userPage = sysUserMapper.selectPage(page, wrapper);

        return userPage.convert(sysUser -> {
            UserVO vo = new UserVO();
            vo.setId(sysUser.getId());
            vo.setIsCreator(sysUser.getIsCreator());
            vo.setUsername(sysUser.getUsername());
            vo.setNickname(sysUser.getNickname());
            vo.setAvatar(MinioUtils.getPresignedUrl(sysUser.getAvatar()));
            vo.setStatus(sysUser.getStatus());
            vo.setCreateTime(sysUser.getCreateTime());
            return vo;
        });
    }

    private String generateRechargeSn() {
        return "RC" + UUID.randomUUID().toString().replace("-", "");
    }

    @Override
    public RechargeResultVO recharge(Long userId, RechargeResultDTO rechargeResultDTO) throws Exception {
        log.debug("充值 - 金额: {}, 支付方式: {}", rechargeResultDTO.getAmount(), rechargeResultDTO.getPaymentMethod());

        if (rechargeResultDTO.getAmount() > 10000) {
            throw new BaseException(400, "充值金额不能超过10000");
        }
        if (!"ALIPAY".equals(rechargeResultDTO.getPaymentMethod())) {
            throw new BaseException(400, "不支持的支付方式");
        }
        UserRechargeOrder order = new UserRechargeOrder();
        order.setRechargeSn(generateRechargeSn());
        order.setUserId(userId);
        order.setRechargeAmount(rechargeResultDTO.getAmount());
        order.setPayStatus("PENDING");
        order.setPayChannel(rechargeResultDTO.getPaymentMethod());
        userRechargeOrderMapper.insert(order);

        String payUrl = Factory.Payment.Page()
                .pay("用户虚拟币充值", order.getRechargeSn(), order.getRechargeAmount().toString(),
                        alipayConfig.getNotifyUrl())
                .getBody();
        RechargeResultVO rechargeResultVO = new RechargeResultVO();
        rechargeResultVO.setRechargeSn(order.getRechargeSn());
        rechargeResultVO.setPayUrl(payUrl);
        return rechargeResultVO;
    }

    @Transactional(rollbackFor = Exception.class)
    public String alipayNotify(HttpServletRequest request) throws Exception {
        Map<String, String> params = new HashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String key = parameterNames.nextElement();
            params.put(key, request.getParameter(key));
        }

        boolean signVerified = Factory.Payment.Common().verifyNotify(params);
        if (!signVerified) {
            return "fail";
        }

        String orderSn = params.get("out_trade_no");
        String tradeStatus = params.get("trade_status");
        LambdaQueryWrapper<UserRechargeOrder> queryWrapper = Wrappers.lambdaQuery(UserRechargeOrder.class)
                .eq(UserRechargeOrder::getRechargeSn, orderSn);
        UserRechargeOrder order = userRechargeOrderMapper.selectOne(queryWrapper);
        if (order == null) {
            throw new BaseException(400, "订单不存在");
        }

        if (order.getPayStatus().equals("SUCCESS")) {
            return "success";
        }

        if (!order.getPayStatus().equals("PENDING")) {
            return "fail";
        }

        if ("TRADE_SUCCESS".equals(tradeStatus)) {
            order.setPayStatus("SUCCESS");
            userRechargeOrderMapper.updateById(order);

            addBalance(order.getUserId(), BigDecimal.valueOf(order.getRechargeAmount()), "RECHARGE",
                    order.getRechargeSn());

            return "success";
        }

        return "fail";
    }

    @Override
    public IPage<RechargeOrderVO> getRechargePage(Long userId, RechargeOrderQuery query) {
        Page<UserRechargeOrder> page = new Page<>(query.getPageNum(), query.getPageSize());
        Wrapper<UserRechargeOrder> wrapper = Wrappers.<UserRechargeOrder>lambdaQuery()
                .eq(UserRechargeOrder::getUserId, userId)
                .eq(query.getStatus() != null,
                        UserRechargeOrder::getPayStatus,
                        query.getStatus())
                .orderByDesc(UserRechargeOrder::getCreateTime);
        IPage<UserRechargeOrder> pageResult = userRechargeOrderMapper.selectPage(page, wrapper);
        return pageResult.convert(order -> {
            RechargeOrderVO vo = new RechargeOrderVO();
            vo.setOrderSn(order.getRechargeSn());
            vo.setAmount(order.getRechargeAmount());
            vo.setStatus(order.getPayStatus());
            vo.setPayChannel(order.getPayChannel());
            vo.setPayTime(order.getPayTime());
            vo.setCreateTime(order.getCreateTime());
            return vo;
        });
    }

    @Override
    public IPage<CoinTransactionVO> getTransactionPage(Long userId, TransactionPageQuery query) {
        Page<CoinTransaction> page = new Page<>(query.getPageNum(), query.getPageSize());
        Wrapper<CoinTransaction> wrapper = Wrappers.<CoinTransaction>lambdaQuery()
                .eq(CoinTransaction::getUserId, userId)
                .eq(query.getType() != null,
                        CoinTransaction::getType,
                        query.getType())
                .eq(query.getBizType() != null,
                        CoinTransaction::getBizType,
                        query.getBizType())
                .orderByDesc(CoinTransaction::getCreateTime);
        IPage<CoinTransaction> pageResult = coinTransactionMapper.selectPage(page, wrapper);
        return pageResult.convert(
                transaction -> BeanUtil.copyProperties(transaction, CoinTransactionVO.class));
    }
}
