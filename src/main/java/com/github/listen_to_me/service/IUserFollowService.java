package com.github.listen_to_me.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.listen_to_me.domain.entity.UserFollow;

public interface IUserFollowService extends IService<UserFollow> {

    /**
     * 关注创作者
     *
     * @param userId    当前用户ID
     * @param creatorId 创作者ID
     */
    void follow(Long userId, Long creatorId);

    /**
     * 取消关注
     *
     * @param userId    当前用户ID
     * @param creatorId 创作者ID
     */
    void unfollow(Long userId, Long creatorId);
}
