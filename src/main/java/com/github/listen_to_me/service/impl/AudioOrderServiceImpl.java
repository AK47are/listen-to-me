package com.github.listen_to_me.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.listen_to_me.common.exception.BaseException;
import com.github.listen_to_me.common.util.MinioUtils;
import com.github.listen_to_me.common.util.SecurityUtils;
import com.github.listen_to_me.domain.entity.AudioInfo;
import com.github.listen_to_me.domain.query.PageQuery;
import com.github.listen_to_me.domain.vo.AudioOrderDetailVO;
import com.github.listen_to_me.domain.vo.AudioOrderVO;
import com.github.listen_to_me.mapper.AudioInfoMapper;
import com.github.listen_to_me.mapper.CoinTransactionMapper;
import com.github.listen_to_me.mapper.SysUserMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.listen_to_me.domain.entity.AudioOrder;
import com.github.listen_to_me.mapper.AudioOrderMapper;
import com.github.listen_to_me.service.IAudioOrderService;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author kun
 * @since 2026-03-24
 */
@Service
@AllArgsConstructor
public class AudioOrderServiceImpl extends ServiceImpl<AudioOrderMapper, AudioOrder> implements IAudioOrderService {
    private final AudioInfoMapper audioInfoMapper;
    private final AudioOrderMapper audioOrderMapper;
    private final SysUserMapper sysUserMapper;
    private final CoinTransactionMapper coinTransactionMapper;
    private final SysUserServiceImpl sysUserService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AudioOrderVO purchaseAudio(Long audioId) {
        Long userId = SecurityUtils.getCurrentUserId();
        AudioInfo audioInfo = audioInfoMapper.selectById(audioId);
        if (audioInfo == null) {
            throw new BaseException(404, "音频不存在或已下架");
        }
        if(!audioInfo.getIsPaid()) {
            throw new BaseException(400, "该音频免费，无需购买");
        }
        LambdaQueryWrapper<AudioOrder> queryWrapper = Wrappers.lambdaQuery(AudioOrder.class)
                .eq(AudioOrder::getUserId, userId)
                .eq(AudioOrder::getAudioId, audioId)
                .eq(AudioOrder::getPayStatus, 1);
        AudioOrder audioOrder = audioOrderMapper.selectOne(queryWrapper);
        if(audioOrder != null) {
            throw new BaseException(409, "你已经购买过该音频");
        }
        String orderSn = "order-" + System.currentTimeMillis();
        sysUserService.deductBalance(userId, audioInfo.getPrice(), "AUDIO", orderSn);

        AudioOrder order = new AudioOrder();
        order.setUserId(userId);
        order.setAudioId(audioId);
        order.setPayAmount(audioInfo.getPrice());
        order.setPayStatus(1);
        order.setOrderSn(orderSn);
        order.setPayTime(LocalDateTime.now());
        audioOrderMapper.insert(order);

        AudioOrderVO vo = new AudioOrderVO();
        vo.setOrderSn(order.getOrderSn());
        vo.setAudioTitle(audioInfo.getTitle());
        vo.setCoverUrl(MinioUtils.getPresignedUrl(audioInfo.getCoverPath()));
        vo.setPayAmount(audioInfo.getPrice());
        vo.setStatus("SUCCESS");
        vo.setCreateTime(order.getPayTime());
        return vo;
    }

    @Override
    public AudioOrderDetailVO queryAudioOrderDetail(String sn) {
        LambdaQueryWrapper<AudioOrder> queryWrapper = Wrappers.lambdaQuery(AudioOrder.class)
                .eq(AudioOrder::getOrderSn, sn);
        AudioOrder order = audioOrderMapper.selectOne(queryWrapper);
        AudioInfo audioInfo = audioInfoMapper.selectById(order.getAudioId());
        if(order == null) {
            throw new BaseException(404, "订单不存在");
        }
        AudioOrderDetailVO vo = new AudioOrderDetailVO();
        vo.setOrderSn(order.getOrderSn());
        vo.setAudioTitle(audioInfo.getTitle());
        vo.setCoverUrl(MinioUtils.getPresignedUrl(MinioUtils.getPresignedUrl(audioInfo.getCoverPath())));
        vo.setPayAmount(order.getPayAmount());
        vo.setStatus(order.getPayStatus() == 1 ? "SUCCESS" : "FAILED");
        vo.setCreateTime(order.getCreateTime());
        vo.setPayTime(order.getPayTime());
        return vo;
    }

    @Override
    public IPage<AudioOrderVO> queryAudioOrderPage(PageQuery query) {
        Long userId = SecurityUtils.getCurrentUserId();
        IPage<AudioOrderVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<AudioOrderVO> audioOrderPage = audioOrderMapper.selectPageByUserId(page, userId);
        audioOrderPage.getRecords().forEach(vo -> {
            vo.setCoverUrl(MinioUtils.getPresignedUrl(vo.getCoverUrl()));
        });
        return audioOrderPage;
    }
}