package com.github.listen_to_me.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.listen_to_me.common.exception.BaseException;
import com.github.listen_to_me.domain.entity.Notification;
import com.github.listen_to_me.domain.query.PageQuery;
import com.github.listen_to_me.mapper.NotificationMapper;
import com.github.listen_to_me.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public void send(Long recipientId, String type, String title, String content, Long relatedId) {
        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setIsRead(false);
        notificationMapper.insert(notification);
        log.info("通知已发送 - 接收者: {}, 类型: {}", recipientId, type);
    }

    @Override
    public IPage<Notification> getPage(Long userId, PageQuery pageQuery) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getRecipientId, userId)
                .orderByDesc(Notification::getCreateTime);
        return notificationMapper.selectPage(
                new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize()), wrapper);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getId, notificationId)
                .eq(Notification::getRecipientId, userId)
                .set(Notification::getIsRead, true);
        boolean updated = notificationMapper.update(null, wrapper) > 0;
        if (!updated) {
            if (!notificationMapper.exists(
                    new LambdaQueryWrapper<Notification>().eq(Notification::getId, notificationId))) {
                throw new BaseException(400, "通知不存在");
            }
            throw new BaseException(403, "无权操作该通知");
        }
    }
}
