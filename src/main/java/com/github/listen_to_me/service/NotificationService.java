package com.github.listen_to_me.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.listen_to_me.domain.entity.Notification;
import com.github.listen_to_me.domain.query.PageQuery;

public interface NotificationService {

    void send(Long recipientId, String type, String title, String content, Long relatedId);

    IPage<Notification> getPage(Long userId, PageQuery pageQuery);

    void markAsRead(Long notificationId, Long userId);
}
